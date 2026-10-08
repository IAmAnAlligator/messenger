import {
    createContext,
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


type ChatsContextValue = {

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
        "MESSAGE_DELETED",
        "MESSAGE_READ"
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
        chatEventsVersion,
        setChatEventsVersion
    ] = useState(0);


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


                        if (
                            !CHAT_LIST_EVENTS.has(
                                event.type
                            )
                        ) {
                            return;
                        }


                        /*
                         * WebSocket is only a signal
                         * that backend data changed.
                         *
                         * The actual chat state,
                         * unreadCount and message status
                         * are loaded from backend.
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


    useEffect(
        () => {

            setChatEventsVersion(0);

        },
        [
            userId
        ]
    );


    const value: ChatsContextValue = {

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
