import { Client } from "@stomp/stompjs";

import type {
    IMessage,
    StompSubscription
} from "@stomp/stompjs";


type SubscriptionCallback =
    (message: IMessage) => void;


type ConnectionListener =
    () => void;


let client: Client | null = null;


const subscriptions =
    new Map<
        string,
        Set<SubscriptionCallback>
    >();


const activeSubs =
    new Map<
        string,
        StompSubscription
    >();


const connectionListeners =
    new Set<ConnectionListener>();


let reconnectAttempts = 0;


const MAX_RECONNECT = 5;


export function connectSocket(
    token: string
) {

    /*
     * Если socket уже создан и активен,
     * повторно Client не создаём.
     */

    if (client?.active) {
        return client;
    }


    client =
        new Client({

            brokerURL:
                "ws://localhost:8080/ws",

            reconnectDelay:
                3000,

            connectHeaders: {
                Authorization:
                    `Bearer ${token}`
            },

            debug: message => {

                console.log(
                    "[STOMP]",
                    message
                );

            },

            beforeConnect: () => {

                console.log(
                    "[WS] connecting..."
                );

            },

            onConnect: () => {

                console.log(
                    "[WS] connected"
                );


                reconnectAttempts = 0;


                /*
                 * После каждого успешного подключения
                 * восстанавливаем все подписки.
                 *
                 * Это работает как для первого connect,
                 * так и для reconnect.
                 */

                resubscribeAll();


                /*
                 * Уведомляем всех подписчиков,
                 * например useChatSocket.
                 *
                 * Важно:
                 * здесь нет chatId и reloadMessages.
                 * WebSocket-сервис ничего не знает
                 * о конкретном чате.
                 */

                connectionListeners.forEach(
                    listener => {

                        try {

                            listener();

                        } catch (error) {

                            console.error(
                                "[WS CONNECTION LISTENER ERROR]",
                                error
                            );

                        }

                    }
                );

            },

            onDisconnect: () => {

                console.log(
                    "[WS] disconnected"
                );

            },

            onWebSocketClose: () => {

                reconnectAttempts++;


                console.log(
                    "[WS] closed attempt:",
                    reconnectAttempts
                );


                if (
                    reconnectAttempts >=
                    MAX_RECONNECT
                ) {

                    console.warn(
                        "[WS] max reconnect reached"
                    );


                    disconnectSocket();

                }

            },

            onWebSocketError: error => {

                console.error(
                    "[WS ERROR]",
                    error
                );

            },

            onStompError: frame => {

                console.error(
                    "[STOMP ERROR]",
                    frame
                );


                /*
                 * STOMP ERROR означает,
                 * что сервер отклонил CONNECT
                 * или произошла фатальная ошибка.
                 *
                 * В этом случае reconnect не продолжаем.
                 */

                disconnectSocket();


                localStorage.removeItem(
                    "accessToken"
                );


                window.location.href =
                    "/login";

            }

        });


    client.activate();


    return client;

}


/*
 * Регистрирует listener, который будет вызван
 * после каждого успешного подключения:
 *
 * - initial connect
 * - reconnect
 *
 * Возвращает функцию удаления listener.
 */

export function onSocketConnected(
    listener: ConnectionListener
) {

    connectionListeners.add(
        listener
    );


    if (client?.connected) {

        try {

            listener();

        } catch (error) {

            console.error(
                "[WS CONNECTION LISTENER ERROR]",
                error
            );

        }

    }


    return () => {

        connectionListeners.delete(
            listener
        );

    };

}


export function subscribe(
    destination: string,
    callback: SubscriptionCallback
) {

    let callbacks =
        subscriptions.get(
            destination
        );


    if (!callbacks) {

        callbacks =
            new Set();

        subscriptions.set(
            destination,
            callbacks
        );

    }


    callbacks.add(
        callback
    );


    /*
     * Если socket уже подключён,
     * создаём STOMP subscription сразу.
     */

    if (client?.connected) {

        createSubscription(
            destination
        );

    }

}


export function unsubscribe(
    destination: string,
    callback: SubscriptionCallback
) {

    const callbacks =
        subscriptions.get(
            destination
        );


    if (!callbacks) {
        return;
    }


    callbacks.delete(
        callback
    );


    /*
     * Если для destination больше
     * нет callback'ов — удаляем
     * саму STOMP subscription.
     */

    if (
        callbacks.size === 0
    ) {

        subscriptions.delete(
            destination
        );


        const subscription =
            activeSubs.get(
                destination
            );


        if (subscription) {

            subscription.unsubscribe();

            activeSubs.delete(
                destination
            );

        }

    }

}


function createSubscription(
    destination: string
) {

    if (!client?.connected) {
        return;
    }


    /*
     * На один destination создаём
     * только одну STOMP subscription.
     *
     * Несколько React-компонентов могут
     * использовать один destination —
     * callbacks будут храниться в Set.
     */

    const existing =
        activeSubs.get(
            destination
        );


    if (existing) {
        return;
    }


    const subscription =
        client.subscribe(
            destination,
            message => {

                const callbacks =
                    subscriptions.get(
                        destination
                    );


                if (!callbacks) {
                    return;
                }


                callbacks.forEach(
                    callback => {

                        try {

                            callback(
                                message
                            );

                        } catch (error) {

                            console.error(
                                "[WS CALLBACK ERROR]",
                                error
                            );

                        }

                    }
                );

            }
        );


    activeSubs.set(
        destination,
        subscription
    );

}


function resubscribeAll() {

    if (!client?.connected) {
        return;
    }


    /*
     * Старые STOMP subscriptions
     * больше не считаем активными.
     */

    activeSubs.forEach(
        subscription => {

            try {

                subscription.unsubscribe();

            } catch (error) {

                console.error(
                    "[WS UNSUBSCRIBE ERROR]",
                    error
                );

            }

        }
    );


    activeSubs.clear();


    /*
     * subscriptions содержит логические
     * подписки приложения.
     *
     * Восстанавливаем их на новом
     * STOMP connection.
     */

    for (
        const destination
        of subscriptions.keys()
    ) {

        createSubscription(
            destination
        );

    }

}


export function disconnectSocket() {

    /*
     * Удаляем реальные STOMP subscriptions.
     */

    activeSubs.forEach(
        subscription => {

            try {

                subscription.unsubscribe();

            } catch (error) {

                console.error(
                    "[WS UNSUBSCRIBE ERROR]",
                    error
                );

            }

        }
    );


    activeSubs.clear();


    /*
     * Удаляем логические subscriptions.
     */

    subscriptions.clear();


    /*
     * Удаляем listeners подключения.
     */

    connectionListeners.clear();


    reconnectAttempts = 0;


    if (client) {

        /*
         * deactivate() завершает STOMP client.
         */

        client.deactivate();

    }


    client = null;

}


export function getSocket() {

    return client;

}