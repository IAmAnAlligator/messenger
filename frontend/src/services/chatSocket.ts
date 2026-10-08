import { Client } from "@stomp/stompjs";

import type {
    IMessage,
    StompSubscription
} from "@stomp/stompjs";

import {
    refreshToken
} from "./refresh";


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


let refreshPromise:
    Promise<string> | null = null;


function getTokenExpiration(
    token: string
): number | null {

    try {

        const payload =
            token.split(".")[1];

        if (!payload) {
            return null;
        }


        const decoded =
            JSON.parse(
                atob(
                    payload
                        .replace(/-/g, "+")
                        .replace(/_/g, "/")
                )
            );


        if (
            typeof decoded.exp !== "number"
        ) {

            return null;

        }


        return decoded.exp * 1000;

    } catch {

        return null;

    }

}


async function getValidAccessToken(): Promise<string> {

    const token =
        localStorage.getItem(
            "accessToken"
        );


    if (!token) {

        throw new Error(
            "Access token not found"
        );

    }


    const expiration =
        getTokenExpiration(
            token
        );


    if (
        expiration === null ||
        expiration <= Date.now()
    ) {

        return refreshAccessToken();

    }


    return token;

}


async function refreshAccessToken(): Promise<string> {

    if (refreshPromise) {

        return refreshPromise;

    }


    refreshPromise =
        refreshToken()
            .then(data => {

                localStorage.setItem(
                    "accessToken",
                    data.accessToken
                );


                return data.accessToken;

            })
            .finally(() => {

                refreshPromise = null;

            });


    return refreshPromise;

}


function forceLogout() {

    localStorage.removeItem(
        "accessToken"
    );


    disconnectSocket();


    window.location.href =
        "/login";

}


export async function connectSocket() {

    if (client?.active) {

        return client;

    }


    client =
        new Client({

            brokerURL:
                "ws://localhost:8080/ws",

            reconnectDelay:
                3000,

            connectHeaders: {},

            debug: message => {

                console.log(
                    "[STOMP]",
                    message
                );

            },

            beforeConnect: async () => {

                console.log(
                    "[WS] connecting..."
                );


                try {

                    const accessToken =
                        await getValidAccessToken();


                    if (!client) {

                        return;

                    }


                    client.connectHeaders = {

                        Authorization:
                            `Bearer ${accessToken}`

                    };

                } catch (error) {

                    console.error(
                        "[WS AUTH ERROR]",
                        error
                    );


                    forceLogout();


                    throw error;

                }

            },

            onConnect: () => {

                console.log(
                    "[WS] connected"
                );


                reconnectAttempts = 0;


                resubscribeAll();


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

            }

        });


    client.activate();


    return client;

}


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


    subscriptions.clear();


    connectionListeners.clear();


    reconnectAttempts = 0;


    if (client) {

        client.deactivate();

    }


    client = null;

}


export function getSocket() {

    return client;

}
