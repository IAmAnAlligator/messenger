
import {
    useCallback,
    useEffect,
    useRef,
    useState
} from "react";


import {
    api
} from "../api/client";


import type {
    UserDto
} from "../types/user";


import {
    useChatsContext
} from "../contexts/ChatsContext";


export type LastMessageStatus =
    | "NONE"
    | "SENT"
    | "READ";


export type ChatMemberDto = {

    user: UserDto;

    chatRole: "ADMIN" | "MEMBER";

    joinedAt: string;

};


export type ChatDto = { id: string; name: string; type: "PRIVATE" | "GROUP"; members: ChatMemberDto[]; createdAt: string; lastMessageAt: string | null; lastMessage: LastMessageDto | null; unreadCount: number; lastMessageStatus: LastMessageStatus; };

export type LastMessageDto = { id: string; sender: UserDto; content: string | null; createdAt: string; };



type ChatCursor = {

    cursorTime: string;

    cursorId: string;

};


type ChatPageResponse = {

    content: ChatDto[];

    nextCursor: ChatCursor | null;

    hasNext: boolean;

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
            chat => !ids.has(chat.id)
        )

    ];

}


export function useChats() {

    const {
        chatEventsVersion
    } = useChatsContext();


    const [
        chats,
        setChats
    ] = useState<ChatDto[]>([]);


    const [
        loading,
        setLoading
    ] = useState(true);


    const [
        loadingMore,
        setLoadingMore
    ] = useState(false);


    const [
        hasNext,
        setHasNext
    ] = useState(false);


    const [
        cursor,
        setCursor
    ] = useState<ChatCursor | null>(null);


    const loadingMoreRef =
        useRef(false);


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
                        response.data.content
                    );


                    setCursor(
                        response.data.nextCursor
                    );


                    setHasNext(
                        response.data.hasNext
                    );

                } catch (error) {

                    console.error(
                        "Failed to load chats:",
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


    const loadMore =
        useCallback(
            async () => {

                if (
                    !hasNext ||
                    !cursor ||
                    loadingMoreRef.current
                ) {
                    return;
                }


                loadingMoreRef.current = true;

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


                    setChats(
                        previous =>
                            mergeChats(
                                previous,
                                response.data.content
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
                        "Failed to load more chats:",
                        error
                    );

                } finally {

                    loadingMoreRef.current = false;

                    setLoadingMore(false);

                }

            },
            [
                cursor,
                hasNext
            ]
        );


    useEffect(
        () => {

            void loadChats();

        },
        [
            loadChats,
            chatEventsVersion
        ]
    );


    return {

        chats,

        loading,

        loadingMore,

        hasNext,

        loadMore

    };

}

