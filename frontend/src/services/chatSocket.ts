import { Client } from "@stomp/stompjs";
import type {
    IMessage,
    StompSubscription
} from "@stomp/stompjs";

type SubscriptionCallback =
    (message: IMessage) => void;

let client: Client | null = null;

/*
 * Один destination -> много callback
 */
const subscriptions =
    new Map<string, Set<SubscriptionCallback>>();

/*
 * Активные STOMP подписки
 */
const activeSubs =
    new Map<string, StompSubscription>();

let reconnectAttempts = 0;

const MAX_RECONNECT = 5;

export function connectSocket(
    token: string,
    onConnect?: () => void
) {

    if (client?.active || client?.connected) {
        return client;
    }

    client = new Client({

        brokerURL: "ws://localhost:8080/ws",

        reconnectDelay: 3000,

        connectHeaders: {
            Authorization: `Bearer ${token}`
        },

        debug: msg => {
            console.log("[STOMP]", msg);
        },

        beforeConnect: () => {
            console.log("[WS] connecting...");
        },

        onConnect: () => {

            console.log("[WS] connected");

            reconnectAttempts = 0;

            resubscribeAll();

            onConnect?.();
        },

        onDisconnect: () => {
            console.log("[WS] disconnected");
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

        onWebSocketError: err => {
            console.error(
                "[WS ERROR]",
                err
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

/*
 * subscribe
 */

export function subscribe(
    destination: string,
    callback: SubscriptionCallback
) {

    let callbacks =
        subscriptions.get(destination);

    if (!callbacks) {

        callbacks = new Set();

        subscriptions.set(
            destination,
            callbacks
        );

    }

    callbacks.add(callback);

    if (client?.connected) {
        createOrReplace(destination);
    }

}

/*
 * unsubscribe
 */

export function unsubscribe(
    destination: string,
    callback?: SubscriptionCallback
) {

    if (!callback) {

        subscriptions.delete(destination);

        const sub =
            activeSubs.get(destination);

        if (sub) {

            sub.unsubscribe();

            activeSubs.delete(destination);

        }

        return;
    }

    const callbacks =
        subscriptions.get(destination);

    if (!callbacks) {
        return;
    }

    callbacks.delete(callback);

    if (callbacks.size === 0) {

        subscriptions.delete(destination);

        const sub =
            activeSubs.get(destination);

        if (sub) {

            sub.unsubscribe();

            activeSubs.delete(destination);

        }

    }

}

function createOrReplace(
    destination: string
) {

    const existing =
        activeSubs.get(destination);

    if (existing) {
        existing.unsubscribe();
    }

    const sub =
        client!.subscribe(
            destination,
            message => {

                const callbacks =
                    subscriptions.get(
                        destination
                    );

                if (!callbacks) {
                    return;
                }

                callbacks.forEach(cb => {

                    try {

                        cb(message);

                    } catch (e) {

                        console.error(
                            "[WS CALLBACK ERROR]",
                            e
                        );

                    }

                });

            }
        );

    activeSubs.set(
        destination,
        sub
    );

}

function resubscribeAll() {

    if (!client?.connected) {
        return;
    }

    for (const destination of subscriptions.keys()) {
        createOrReplace(destination);
    }

}

/*
 * Полностью закрыть сокет
 */

export function disconnectSocket() {

    activeSubs.forEach(
        sub => sub.unsubscribe()
    );

    activeSubs.clear();

    subscriptions.clear();

    reconnectAttempts = 0;

    client?.deactivate();

    client = null;

}

export function getSocket() {
    return client;
}