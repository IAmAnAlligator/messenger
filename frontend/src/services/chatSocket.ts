import { Client } from "@stomp/stompjs";
import type {
    IMessage,
    StompSubscription
} from "@stomp/stompjs";


type SubscriptionCallback =
    (message: IMessage) => void;


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


let reconnectAttempts = 0;

const MAX_RECONNECT = 5;


export function connectSocket(
    token: string,
    onConnect?: () => void
) {

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

                resubscribeAll();

                onConnect?.();

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


    activeSubs.forEach(
        subscription =>
            subscription.unsubscribe()
    );


    activeSubs.clear();


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

    activeSubs.forEach(
        subscription =>
            subscription.unsubscribe()
    );


    activeSubs.clear();

    subscriptions.clear();

    reconnectAttempts = 0;


    if (client) {

        client.deactivate();

    }


    client = null;

}


export function getSocket() {

    return client;

}