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
    useState(true
    );


const [
    currentUserLastReadMessageId,
    setCurrentUserLastReadMessageId
] =
    useState<number | null>(
        null
    );


const [
    otherUserLastReadMessageId,
    setOtherUserLastReadMessageId
] =
    useState<number | null>(
        null
    );

const [
    otherUsersLastReadMessageIds,
    setOtherUsersLastReadMessageIds
] = useState<Map<number, number>>(
    new Map()
);

const isMessageReadByOtherUser =
    useCallback(
        (messageId: number): boolean => {

            for (
                const lastReadMessageId
                    of otherUsersLastReadMessageIds.values()
            ) {

                if (
                    messageId <=
                    lastReadMessageId
                ) {
                    return true;
                }

            }

            return false;

        },
        [
            otherUsersLastReadMessageIds
        ]
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

                const currentUserId = user?.id;

const otherUsersReadMap =
    new Map<number, number>();

if (currentUserId !== undefined) {

    for (const member of chatData.members) {

        if (
            member.user.id ===
            currentUserId
        ) {
            continue;
        }

        if (
            member.lastReadMessageId !== null
        ) {

            otherUsersReadMap.set(
                member.user.id,
                member.lastReadMessageId
            );

        }

    }

}

setOtherUsersLastReadMessageIds(
    otherUsersReadMap
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


                const currentMember =
                    chatData.members.find(
                        member =>
                            member.user.id ===
                            user?.id
                    );


                setCurrentUserLastReadMessageId(
                    currentMember
                        ?.lastReadMessageId
                        ?? null
                );


const otherMember =
    chatData.members.length === 2
        ? chatData.members.find(
            member => member.user.id !== user?.id
        )
        : undefined;

console.log(
    "[CHAT READ STATE]",
    {
        currentUserId: user?.id,

        members:
            chatData.members.map(
                member => ({
                    userId:
                        member.user.id,
                    username:
                        member.user.username,
                    lastReadMessageId:
                        member.lastReadMessageId
                })
            ),

        otherMember:
            otherMember
                ? {
                    userId:
                        otherMember.user.id,
                    username:
                        otherMember.user.username,
                    lastReadMessageId:
                        otherMember.lastReadMessageId
                }
                : null
    }
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


const markCurrentUserRead =
    useCallback(
        (
            messageId: number
        ) => {

            setCurrentUserLastReadMessageId(
                prev => {

                    if (
                        prev === null ||
                        messageId > prev
                    ) {

                        return messageId;

                    }


                    return prev;

                }
            );

        },
        []
    );


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
        (event: MessageReadEvent) => {

            if (
                event.chatId !== chatId
            ) {
                return;
            }


            if (
                event.readerId === user?.id
            ) {
                return;
            }


            console.log(
                "[READ] other user read:",
                event
            );


            setOtherUsersLastReadMessageIds(
                prev => {

                    const next =
                        new Map(prev);


                    const previous =
                        next.get(
                            event.readerId
                        );


                    if (
                        previous === undefined ||
                        event.lastReadMessageId >
                            previous
                    ) {

                        next.set(
                            event.readerId,
                            event.lastReadMessageId
                        );

                    }


                    return next;

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

    currentUserLastReadMessageId,

    otherUserLastReadMessageId,

    addMessage,

    removeMessage,

    updateMessageStatus,

    markCurrentUserRead,

    reloadMessages,

    loadMoreMessages,

    sendFile,

    isMessageReadByOtherUser

};

}
