import { Client } from "@stomp/stompjs";
import type { IMessage, StompSubscription } from "@stomp/stompjs";

type SubscriptionCallback = (message: IMessage) => void;

let client: Client | null = null;

const subscriptions = new Map<string, SubscriptionCallback>();
const activeSubs = new Map<string, StompSubscription>();

let reconnectAttempts = 0;
const MAX_RECONNECT = 5;

export function connectSocket(
    token: string,
    onConnect?: () => void
) {

    if (client?.connected) return client;

    client = new Client({
        brokerURL: "ws://localhost:8080/ws",

        reconnectDelay: 3000,

        connectHeaders: {
            Authorization: `Bearer ${token}`
        },

        debug: (msg) => {
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

            console.log("[WS] closed attempt:", reconnectAttempts);

            if (reconnectAttempts >= MAX_RECONNECT) {
                console.warn("[WS] max reconnect reached → stopping socket");

                disconnectSocket();
            }
        },

        onWebSocketError: (err) => {
            console.error("[WS ERROR]", err);
        },

        onStompError: (frame) => {
            console.error("[STOMP ERROR]", frame);

            disconnectSocket();
            localStorage.removeItem("accessToken");
            window.location.href = "/login";
        }
    });

    client.activate();

    return client;
}

/**
 * SAFE subscribe
 */
export function subscribe(
    destination: string,
    callback: SubscriptionCallback
) {
    subscriptions.set(destination, callback);

    if (client?.connected) {
        createOrReplace(destination, callback);
    }
}

/**
 * SAFE unsubscribe
 */
export function unsubscribe(destination: string) {

    subscriptions.delete(destination);

    const sub = activeSubs.get(destination);

    if (sub) {
        sub.unsubscribe();
        activeSubs.delete(destination);
    }
}

function createOrReplace(
    destination: string,
    callback: SubscriptionCallback
) {

    const existing = activeSubs.get(destination);

    if (existing) {
        existing.unsubscribe();
    }

    const sub = client!.subscribe(destination, callback);

    activeSubs.set(destination, sub);
}

function resubscribeAll() {

    if (!client?.connected) return;

    for (const [dest, cb] of subscriptions.entries()) {
        createOrReplace(dest, cb);
    }
}

/**
 * FULL cleanup
 */
export function disconnectSocket() {

    activeSubs.forEach(s => s.unsubscribe());
    activeSubs.clear();

    subscriptions.clear();

    reconnectAttempts = 0;

    client?.deactivate();
    client = null;
}

export function getSocket() {
    return client;
}