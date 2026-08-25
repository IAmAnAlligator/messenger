import type {
    IMessage
} from "@stomp/stompjs";

import {
    useCallback,
    useEffect,
    useRef,
    useState
} from "react";

import {
    useNavigate
} from "react-router-dom";

import {
    useAuth
} from "../contexts/AuthContext";

import {
    connectSocket,
    subscribe,
    unsubscribe,
    getSocket
} from "../services/chatSocket";

import type {
    MessageDto
} from "../types/message";

import type {
    MessageReadEvent
} from "../types/chat";

import type {
    ChatSocketEvent,
    WebSocketErrorResponse
} from "../types/events";


interface Props {

    chatId?: number;

    onMessage(
        message: MessageDto
    ): void;

    onDelete(
        messageId: number
    ): void;

    onRead(
        event: MessageReadEvent
    ): void;

    reloadMessages?():
        void | Promise<void>;
}


export function useChatSocket({
    chatId,
    onMessage,
    onDelete,
    onRead,
    reloadMessages
}: Props) {

    const navigate =
        useNavigate();

    const { user } =
        useAuth();


    const [
        error,
        setError
    ] =
        useState<string | null>(
            null
        );


    /*
     * Храним актуальные callbacks.
     *
     * Это позволяет не пересоздавать
     * STOMP subscription при каждом render.
     */

    const onMessageRef =
        useRef(onMessage);

    const onDeleteRef =
        useRef(onDelete);

    const onReadRef =
        useRef(onRead);

    const reloadMessagesRef =
        useRef(reloadMessages);

    const userIdRef =
        useRef(user?.id);


    useEffect(() => {

        onMessageRef.current =
            onMessage;

    }, [onMessage]);


    useEffect(() => {

        onDeleteRef.current =
            onDelete;

    }, [onDelete]);


    useEffect(() => {

        onReadRef.current =
            onRead;

    }, [onRead]);


    useEffect(() => {

        reloadMessagesRef.current =
            reloadMessages;

    }, [reloadMessages]);


    useEffect(() => {

        userIdRef.current =
            user?.id;

    }, [user?.id]);


    /*
     * READ до указанного сообщения.
     */

    const sendReadUpTo =
        useCallback(
            (
                messageId: number
            ) => {

                if (!chatId) {
                    return;
                }


                const socket =
                    getSocket();


                if (!socket?.connected) {

                    console.warn(
                        "[READ] socket is not connected"
                    );

                    return;

                }


                console.log(
                    "[READ] sending",
                    {
                        chatId,
                        messageId
                    }
                );


                socket.publish({

                    destination:
                        "/app/chat.read",

                    body:
                        JSON.stringify({

                            chatId,

                            messageId

                        })

                });

            },
            [
                chatId
            ]
        );


    useEffect(() => {

        if (!chatId) {
            return;
        }


        const token =
            localStorage.getItem(
                "accessToken"
            );


        if (!token) {
            return;
        }


        const chatTopic =
            `/topic/chat/${chatId}`;


        const errorQueue =
            "/user/queue/errors";


        const handleChatMessage =
            (frame: IMessage) => {

                let event:
                    ChatSocketEvent;


                try {

                    event =
                        JSON.parse(
                            frame.body
                        ) as ChatSocketEvent;

                } catch (error) {

                    console.error(
                        "[WS] invalid JSON",
                        error
                    );

                    return;

                }


                switch (event.type) {

                    case "MESSAGE_CREATED": {

                        onMessageRef.current(
                            event.payload
                        );

                        break;
                    }


                    case "MESSAGE_DELETED": {

                        onDeleteRef.current(
                            event.payload.messageId
                        );

                        break;
                    }


                    case "MESSAGE_READ": {

                        const readEvent =
                            event.payload;


                        console.log(
                            "[MESSAGE_READ]",
                            readEvent
                        );


                        /*
                         * ВАЖНО:
                         *
                         * READ должен обрабатываться
                         * у отправителя, когда readerId
                         * является другим пользователем.
                         *
                         * Собственный READ игнорируем
                         * для галочек.
                         */

                        if (
                            readEvent.readerId ===
                            userIdRef.current
                        ) {

                            console.log(
                                "[MESSAGE_READ] own read ignored"
                            );

                            break;

                        }


                        onReadRef.current(
                            readEvent
                        );


                        break;
                    }


                    case "CHAT_DELETED": {

                        if (
                            event.payload.chatId ===
                            chatId
                        ) {

                            navigate(
                                "/chats",
                                {
                                    replace: true
                                }
                            );

                        }

                        break;
                    }


                    case "CHAT_MEMBER_REMOVED": {

                        if (
                            event.payload.chatId !==
                            chatId
                        ) {
                            break;
                        }


                        if (
                            event.payload.userId ===
                            userIdRef.current
                        ) {

                            navigate(
                                "/chats",
                                {
                                    replace: true
                                }
                            );

                            return;

                        }


                        reloadMessagesRef
                            .current
                            ?.();

                        break;
                    }


                    case "CHAT_CREATED":
                    case "CHAT_RENAMED":
                    case "CHAT_MEMBER_ADDED":
                    case "CHAT_MEMBER_LEFT":
                        break;

                }

            };


        const handleError =
            (frame: IMessage) => {

                try {

                    const response =
                        JSON.parse(
                            frame.body
                        ) as WebSocketErrorResponse;


                    setError(
                        response.message
                    );


                    setTimeout(() => {

                        setError(null);

                    }, 4000);

                } catch (error) {

                    console.error(
                        "[WS ERROR PARSE]",
                        error
                    );

                }

            };


        /*
         * Подключаем socket.
         *
         * Не передаём reloadMessages в dependency
         * effect.
         */

        connectSocket(
            token,
            () => {

                console.log(
                    "[WS] chat socket connected",
                    chatId
                );

                reloadMessagesRef
                    .current
                    ?.();

            }
        );


        subscribe(
            chatTopic,
            handleChatMessage
        );


        subscribe(
            errorQueue,
            handleError
        );


        return () => {

            unsubscribe(
                chatTopic,
                handleChatMessage
            );


            unsubscribe(
                errorQueue,
                handleError
            );

        };


    }, [
        chatId,
        navigate
    ]);


    const sendMessage =
        useCallback(
            (
                content: string
            ) => {

                const socket =
                    getSocket();


                if (
                    !socket?.connected ||
                    !chatId
                ) {

                    console.warn(
                        "[MESSAGE] socket is not connected"
                    );

                    return;

                }


                socket.publish({

                    destination:
                        "/app/chat.send",

                    body:
                        JSON.stringify({

                            chatId,

                            content

                        })

                });

            },
            [
                chatId
            ]
        );


    const deleteMessage =
        useCallback(
            (
                messageId: number
            ) => {

                const socket =
                    getSocket();


                if (
                    !socket?.connected ||
                    !chatId
                ) {
                    return;
                }


                socket.publish({

                    destination:
                        "/app/chat.delete",

                    body:
                        JSON.stringify({

                            id:
                                messageId,

                            chatId

                        })

                });

            },
            [
                chatId
            ]
        );


    return {

        error,

        sendMessage,

        deleteMessage,

        sendReadUpTo

    };

}