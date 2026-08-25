import {
    useCallback,
    useEffect,
    useState
} from "react";

import {
    getChat
} from "../services/chatService";

import {
    getMessages,
    sendFile as sendFileRequest
} from "../services/messageService";

import {
    useAuth
} from "../contexts/AuthContext";

import type {
    ChatDto,
    MessageReadEvent
} from "../types/chat";

import type {
    MessageDto
} from "../types/message";

import type {
    CursorDto
} from "../types/pagination";


export function useChat(
    chatId?: number
) {

    const { user } =
        useAuth();


    const [
        chat,
        setChat
    ] =
        useState<ChatDto | null>(
            null
        );


    const [
        messages,
        setMessages
    ] =
        useState<MessageDto[]>(
            []
        );


    const [
        loading,
        setLoading
    ] =
        useState(true);


    const [
        loadingMore,
        setLoadingMore
    ] =
        useState(false);


    const [
        cursor,
        setCursor
    ] =
        useState<CursorDto | null>(
            null
        );


    const [
        hasMore,
        setHasMore
    ] =
        useState(true);


    /*
     * Последнее сообщение,
     * которое прочитал другой участник.
     *
     * Именно это значение используется
     * для двойных галочек у наших сообщений.
     */

    const [
        otherUserLastReadMessageId,
        setOtherUserLastReadMessageId
    ] =
        useState<number | null>(
            null
        );


    const load =
        useCallback(
            async () => {

                if (!chatId) {
                    return;
                }


                try {

                    setLoading(true);


                    const [
                        chatData,
                        messagesPage
                    ] =
                        await Promise.all([

                            getChat(
                                chatId
                            ),

                            getMessages(
                                chatId
                            )

                        ]);


                    setChat(
                        chatData
                    );


                    setMessages(
                        [
                            ...messagesPage.content
                        ].reverse()
                    );


                    setCursor(
                        messagesPage.nextCursor
                    );


                    setHasMore(
                        messagesPage.hasNext
                    );


                    /*
                     * Получаем READ другого пользователя
                     * из ChatMemberDto.
                     */

                    const otherMember =
                        chatData.members.find(
                            member =>
                                member.user.id !==
                                user?.id
                        );


                    setOtherUserLastReadMessageId(
                        otherMember
                            ?.lastReadMessageId
                            ?? null
                    );


                } finally {

                    setLoading(false);

                }

            },
            [
                chatId,
                user?.id
            ]
        );


    useEffect(() => {

        load();

    }, [load]);


    const sendFile =
        useCallback(
            async (
                file: File
            ) => {

                if (!chatId) {
                    return;
                }


                await sendFileRequest(
                    chatId,
                    file
                );

            },
            [
                chatId
            ]
        );


    const loadMoreMessages =
        useCallback(
            async () => {

                if (
                    !chatId ||
                    !hasMore ||
                    loadingMore ||
                    !cursor
                ) {
                    return;
                }


                try {

                    setLoadingMore(true);


                    const page =
                        await getMessages(
                            chatId,
                            cursor
                        );


                    setMessages(prev => {

                        const oldIds =
                            new Set(
                                prev.map(
                                    message =>
                                        message.id
                                )
                            );


                        const newMessages =
                            [
                                ...page.content
                            ]
                                .reverse()
                                .filter(
                                    message =>
                                        !oldIds.has(
                                            message.id
                                        )
                                );


                        return [
                            ...newMessages,
                            ...prev
                        ];

                    });


                    setCursor(
                        page.nextCursor
                    );


                    setHasMore(
                        page.hasNext
                    );


                } catch (error) {

                    console.error(
                        "Failed to load more messages",
                        error
                    );

                } finally {

                    setLoadingMore(false);

                }

            },
            [
                chatId,
                cursor,
                hasMore,
                loadingMore
            ]
        );


    const reloadMessages =
        useCallback(
            async () => {

                if (!chatId) {
                    return;
                }


                try {

                    const page =
                        await getMessages(
                            chatId
                        );


                    setMessages(
                        [
                            ...page.content
                        ].reverse()
                    );


                    setCursor(
                        page.nextCursor
                    );


                    setHasMore(
                        page.hasNext
                    );


                } catch (error) {

                    console.error(
                        "Failed to reload messages",
                        error
                    );

                }

            },
            [
                chatId
            ]
        );


    const addMessage =
        useCallback(
            (
                message: MessageDto
            ) => {

                setMessages(prev => {

                    if (
                        prev.some(
                            current =>
                                current.id ===
                                message.id
                        )
                    ) {

                        return prev;

                    }


                    return [
                        ...prev,
                        message
                    ];

                });

            },
            []
        );


    const removeMessage =
        useCallback(
            (
                messageId: number
            ) => {

                setMessages(prev =>
                    prev.filter(
                        message =>
                            message.id !==
                            messageId
                    )
                );

            },
            []
        );


    const updateMessageStatus =
        useCallback(
            (
                event: MessageReadEvent
            ) => {

                if (
                    event.chatId !==
                    chatId
                ) {
                    return;
                }


                /*
                 * Если читатель — текущий пользователь,
                 * это НЕ должно менять галочки наших
                 * исходящих сообщений.
                 */

                if (
                    event.readerId ===
                    user?.id
                ) {

                    return;

                }


                console.log(
                    "[READ] other user read:",
                    event
                );


                setOtherUserLastReadMessageId(
                    prev => {

                        if (
                            prev === null ||
                            event.lastReadMessageId >
                                prev
                        ) {

                            return event
                                .lastReadMessageId;

                        }


                        return prev;

                    }
                );

            },
            [
                chatId,
                user?.id
            ]
        );


    return {

        chat,

        messages,

        loading,

        loadingMore,

        hasMore,

        otherUserLastReadMessageId,

        addMessage,

        removeMessage,

        updateMessageStatus,

        reloadMessages,

        loadMoreMessages,

        sendFile

    };

}