import {
    createContext,
    useCallback,
    useContext,
    useEffect,
    useState
} from "react";

import type {
    ReactNode
} from "react";

import type {
    IMessage
} from "@stomp/stompjs";

import {
    connectSocket,
    subscribe,
    unsubscribe
} from "../services/chatSocket";

import {
    useAuth
} from "./AuthContext";


type WebSocketEvent<T> = {

    type: string;

    payload: T;

};


type MessageCreatedPayload = {

    id: string;

    chatId: string;

    sender: {

        id: string;

        handle: string;

        username: string;

        role: string;

    };

    content: string;

    createdAt: string;

    attachment: unknown | null;

};


type ChatEventPayload = {

    chatId?: string;

    id?: string;

};


type ChatsContextValue = {

    isChatUnread(
        chatId: string
    ): boolean;

    markChatAsRead(
        chatId: string
    ): void;

    chatEventsVersion: number;

};


const ChatsContext =
    createContext<ChatsContextValue | null>(
        null
    );


type Props = {

    children: ReactNode;

};


const CHAT_LIST_EVENTS =
    new Set([
        "CHAT_CREATED",
        "CHAT_RENAMED",
        "CHAT_DELETED",
        "CHAT_MEMBER_ADDED",
        "CHAT_MEMBER_REMOVED",
        "CHAT_MEMBER_LEFT",
        "MESSAGE_CREATED",
        "MESSAGE_DELETED"
    ]);


export function ChatsProvider({
    children
}: Props) {

    const {
        user
    } = useAuth();


    const userId =
        user?.id ?? null;


    const [
        unreadChatIds,
        setUnreadChatIds
    ] = useState<Set<string>>(
        () => new Set()
    );


    const [
        chatEventsVersion,
        setChatEventsVersion
    ] = useState(0);


    const markChatAsRead =
        useCallback(
            (chatId: string) => {

                setUnreadChatIds(
                    previous => {

                        if (
                            !previous.has(chatId)
                        ) {
                            return previous;
                        }


                        const next =
                            new Set(previous);


                        next.delete(chatId);


                        return next;

                    }
                );

            },
            []
        );


    const isChatUnread =
        useCallback(
            (
                chatId: string
            ): boolean => {

                return unreadChatIds.has(
                    chatId
                );

            },
            [
                unreadChatIds
            ]
        );


    useEffect(
        () => {

            if (!userId) {
                return;
            }


            const token =
                localStorage.getItem(
                    "accessToken"
                );


            if (!token) {
                return;
            }


            connectSocket(token);


            const userTopic =
                `/topic/user/${userId}/chats`;


            const handleUserChats =
                (message: IMessage) => {

                    try {

                        const event =
                            JSON.parse(
                                message.body
                            ) as WebSocketEvent<unknown>;


                        /*
                         * Ignore events that do not
                         * affect the chat list.
                         */
                        if (
                            !CHAT_LIST_EVENTS.has(
                                event.type
                            )
                        ) {
                            return;
                        }


                        /*
                         * MESSAGE_CREATED needs
                         * special handling because
                         * it controls the unread
                         * indicator.
                         */
                        if (
                            event.type ===
                            "MESSAGE_CREATED"
                        ) {

                            const payload =
                                event.payload as MessageCreatedPayload;


                            if (
                                !payload?.chatId ||
                                !payload.sender?.id
                            ) {
                                return;
                            }


                            /*
                             * The sender also receives
                             * their own MESSAGE_CREATED.
                             *
                             * Do not mark own messages
                             * as unread.
                             */
                            if (
                                payload.sender.id ===
                                userId
                            ) {

                                /*
                                 * The chat list itself
                                 * still needs updating
                                 * because lastMessageAt
                                 * may have changed.
                                 */
                                setChatEventsVersion(
                                    previous =>
                                        previous + 1
                                );

                                return;
                            }


                            setUnreadChatIds(
                                previous => {

                                    if (
                                        previous.has(
                                            payload.chatId
                                        )
                                    ) {
                                        return previous;
                                    }


                                    const next =
                                        new Set(
                                            previous
                                        );


                                    next.add(
                                        payload.chatId
                                    );


                                    return next;

                                }
                            );


                            /*
                             * Notify ChatsContainer
                             * that the chat list must
                             * be reloaded.
                             */
                            setChatEventsVersion(
                                previous =>
                                    previous + 1
                            );


                            return;
                        }


                        /*
                         * CHAT_DELETED:
                         *
                         * The deleted chat must no
                         * longer be marked unread.
                         */
                        if (
                            event.type ===
                            "CHAT_DELETED"
                        ) {

                            const payload =
                                event.payload as ChatEventPayload;


                            const chatId =
                                payload?.chatId ??
                                payload?.id;


                            if (chatId) {

                                setUnreadChatIds(
                                    previous => {

                                        if (
                                            !previous.has(
                                                chatId
                                            )
                                        ) {
                                            return previous;
                                        }


                                        const next =
                                            new Set(
                                                previous
                                            );


                                        next.delete(
                                            chatId
                                        );


                                        return next;

                                    }
                                );

                            }

                        }


                        /*
                         * All other chat-list
                         * events cause the list
                         * to be reloaded.
                         */
                        setChatEventsVersion(
                            previous =>
                                previous + 1
                        );

                    } catch (error) {

                        console.error(
                            "Failed to process user chat event:",
                            error
                        );

                    }

                };


            subscribe(
                userTopic,
                handleUserChats
            );


            return () => {

                unsubscribe(
                    userTopic,
                    handleUserChats
                );

            };

        },
        [
            userId
        ]
    );


    /*
     * Logout / user change:
     *
     * unread state belongs to the
     * current authenticated user.
     */
    useEffect(
        () => {

            if (!userId) {

                setUnreadChatIds(
                    new Set()
                );

            }

        },
        [
            userId
        ]
    );


    const value: ChatsContextValue = {

        isChatUnread,

        markChatAsRead,

        chatEventsVersion

    };


    return (

        <ChatsContext.Provider
            value={value}
        >

            {children}

        </ChatsContext.Provider>

    );

}


export function useChatsContext() {

    const context =
        useContext(
            ChatsContext
        );


    if (!context) {

        throw new Error(
            "useChatsContext must be used inside ChatsProvider"
        );

    }


    return context;

}
