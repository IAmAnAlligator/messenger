import {
    Client
} from "@stomp/stompjs";

let client:
    Client
    | null =
    null;

export function connectSocket(
    token: string,
    onConnect?: () => void
) {

    if (
        client?.connected
    ) {

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

            debug:
                message => {

                    console.log(
                        "[STOMP]",
                        message
                    );

                },

            beforeConnect:
                () => {

                    console.log(
                        "[WS] opening"
                    );

                },

            onConnect:
                () => {

                    console.log(
                        "[WS] connected"
                    );

                    onConnect?.();

                },

            onDisconnect:
                () => {

                    console.log(
                        "[WS] disconnected"
                    );

                },

            onWebSocketClose:
                event => {

                    console.log(
                        "[WS] closed",
                        event
                    );

                },

            onWebSocketError:
                error => {

                    console.error(
                        "[WS] error",
                        error
                    );

                },

            onStompError:
                frame => {

                    console.error(
                        "[STOMP] error",
                        frame
                    );

                }

        });

    client.activate();

    return client;

}

export function disconnectSocket() {

    client?.deactivate();

}

export function getSocket() {

    return client;

}