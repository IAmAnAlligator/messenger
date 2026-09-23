import {
useCallback,
useEffect,
useRef,
useState
} from "react";

import type {
IMessage
} from "@stomp/stompjs";

import { api } from "../api/client";

import {
connectSocket,
subscribe,
unsubscribe
} from "../services/chatSocket";

import { useAuth } from "../contexts/AuthContext";

export type UserDto = {

id: number;

username: string;

role: string;

};

export type ChatMemberDto = {

user: UserDto;

chatRole: string;

joinedAt: string;

};

export type ChatDto = {

id: number;

name: string;

type: "PRIVATE" | "GROUP";

members: ChatMemberDto[];

createdAt: string;

lastMessageAt: string | null;

};

type ChatCursor = {

cursorTime: string;

cursorId: number;

};

type ChatPageResponse = {

content: ChatDto[];

nextCursor: ChatCursor | null;

hasNext: boolean;

};

type WebSocketEvent<T> = {

type: string;

payload: T;

};

function mergeChats(
oldChats: ChatDto[],
newChats: ChatDto[]
): ChatDto[] {

const ids =
    new Set(
        oldChats.map(
            chat => chat.id
        )
    );

return [
    ...oldChats,

    ...newChats.filter(
        chat =>
            !ids.has(chat.id)
    )
];

}

export function useChats() {

const { user } =
    useAuth();


const [chats, setChats] =
    useState<ChatDto[]>([]);


const [loading, setLoading] =
    useState(true);


const [loadingMore, setLoadingMore] =
    useState(false);


const [hasNext, setHasNext] =
    useState(true);


const [cursor, setCursor] =
    useState<ChatCursor | null>(null);


const loadingMoreRef =
    useRef(false);


/*
 * Загружает первую страницу.
 *
 * Используется:
 * - при первоначальной загрузке;
 * - после realtime-события изменения списка чатов.
 *
 * При realtime обновлении cursor pagination
 * начинается заново с первой страницы.
 */
const loadChats =
    useCallback(
        async () => {

            try {

                setLoading(true);

                const response =
                    await api.get<ChatPageResponse>(
                        "/chats",
                        {
                            params: {
                                limit: 30
                            }
                        }
                    );

                setChats(
                    response.data.content ?? []
                );

                setCursor(
                    response.data.nextCursor
                );

                setHasNext(
                    response.data.hasNext
                );

            } catch (error) {

                console.error(
                    "[loadChats] error",
                    error
                );

                setChats([]);

                setCursor(null);

                setHasNext(false);

            } finally {

                setLoading(false);

            }

        },
        []
    );


/*
 * Загружает следующую страницу
 * cursor pagination.
 */
const loadMore =
    useCallback(
        async () => {

            if (
                loadingMoreRef.current ||
                loadingMore ||
                !hasNext ||
                !cursor
            ) {
                return;
            }


            loadingMoreRef.current =
                true;

            setLoadingMore(true);


            try {

                const response =
                    await api.get<ChatPageResponse>(
                        "/chats",
                        {
                            params: {

                                cursorTime:
                                    cursor.cursorTime,

                                cursorId:
                                    cursor.cursorId,

                                limit: 30

                            }
                        }
                    );


                setChats(prev =>
                    mergeChats(
                        prev,
                        response.data.content ?? []
                    )
                );


                setCursor(
                    response.data.nextCursor
                );

                setHasNext(
                    response.data.hasNext
                );

            } catch (error) {

                console.error(
                    "[loadMore] error",
                    error
                );

            } finally {

                loadingMoreRef.current =
                    false;

                setLoadingMore(false);

            }

        },
        [
            cursor,
            hasNext,
            loadingMore
        ]
    );


/*
 * Первоначальная загрузка списка чатов.
 */
useEffect(() => {

    void loadChats();

}, [
    loadChats
]);


/*
 * Realtime-обновление списка чатов.
 *
 * Backend отправляет сюда события,
 * которые могут изменить персональный
 * список текущего пользователя:
 *
 * CHAT_CREATED
 * CHAT_DELETED
 * CHAT_RENAMED
 * CHAT_MEMBER_ADDED
 * CHAT_MEMBER_REMOVED
 * CHAT_MEMBER_LEFT
 * MESSAGE_CREATED
 *
 * После любого такого события
 * перечитываем первую страницу через HTTP.
 *
 * Это сохраняет cursor pagination
 * согласованной с backend.
 */
useEffect(() => {

    if (!user) {
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


    const handleUserChats =
        (message: IMessage) => {

            try {

                const event:
                    WebSocketEvent<unknown> =
                    JSON.parse(
                        message.body
                    );


                console.log(
                    "[user chats event]",
                    event.type
                );


                void loadChats();

            } catch (error) {

                console.error(
                    "[user chats event] invalid message",
                    error
                );

            }

        };


    const userTopic =
        `/topic/user/${user.id}/chats`;


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

}, [
    user,
    loadChats
]);


return {

    chats,

    loading,

    loadingMore,

    hasNext,

    loadMore,

    reload: loadChats

};

}