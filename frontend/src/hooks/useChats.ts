import {
    useEffect,
    useState
} from "react";

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

    lastMessageAt: string;

    id: number;

};



type ChatPageResponse = {

    items: ChatDto[];

    nextCursor: ChatCursor | null;

    hasMore: boolean;

};



type ChatMemberEvent = {

    chatId: number;

    userId: number;

};



type WebSocketEvent<T> = {

    type: string;

    payload: T;

};




export function useChats() {


    const { user } = useAuth();



    const [chats, setChats] =
        useState<ChatDto[]>([]);



    const [loading, setLoading] =
        useState(true);



    const [loadingMore, setLoadingMore] =
        useState(false);



    const [hasMore, setHasMore] =
        useState(true);



    const [cursor, setCursor] =
        useState<ChatCursor | null>(null);





    useEffect(() => {

        loadChats();

    }, []);





    useEffect(() => {


        const token =
            localStorage.getItem(
                "accessToken"
            );


        if (!token || !user) {
            return;
        }



        connectSocket(token);





        subscribe(
            "/topic/chat.created",
            () => {

                loadChats();

            }
        );





        subscribe(
            "/topic/chat.deleted",
            message => {


                const event =
                    JSON.parse(message.body);



                setChats(prev =>
                    prev.filter(
                        chat =>
                            chat.id !== event.chatId
                    )
                );


            }
        );





        subscribe(
            `/topic/user/${user.id}/chats`,
            async message => {


                const event:
                    WebSocketEvent<ChatMemberEvent> =
                    JSON.parse(message.body);




                switch(event.type) {



                    case "CHAT_MEMBER_ADDED": {


                        try {


                            const response =
                                await api.get(
                                    `/chats/${event.payload.chatId}`
                                );



                            const chat:ChatDto =
                                response.data;



                            setChats(prev => {


                                if (
                                    prev.some(
                                        c =>
                                            c.id === chat.id
                                    )
                                ) {
                                    return prev;
                                }



                                return [
                                    chat,
                                    ...prev
                                ];


                            });



                        } catch(error) {


                            console.error(error);


                        }


                        break;

                    }




                    case "CHAT_MEMBER_REMOVED": {


                        setChats(prev =>
                            prev.filter(
                                chat =>
                                    chat.id !== event.payload.chatId
                            )
                        );


                        break;

                    }



                    default:
                        break;

                }


            }

        );





        return () => {


            unsubscribe(
                "/topic/chat.created"
            );


            unsubscribe(
                "/topic/chat.deleted"
            );



            unsubscribe(
                `/topic/user/${user.id}/chats`
            );


        };


    }, [user]);








    async function loadChats() {


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
                response.data.items
            );



            setCursor(
                response.data.nextCursor
            );



            setHasMore(
                response.data.hasMore
            );



        } catch(error) {


            console.error(
                error
            );


        } finally {


            setLoading(false);


        }

    }







    async function loadMore() {


        if (
            loadingMore ||
            !hasMore ||
            !cursor
        ) {
            return;
        }





        try {


            setLoadingMore(true);




            const response =
                await api.get<ChatPageResponse>(
                    "/chats",
                    {
                        params: {

                            cursorTime:
                                cursor.lastMessageAt,

                            cursorId:
                                cursor.id,

                            limit:30

                        }
                    }
                );





            setChats(prev => [

                ...prev,

                ...response.data.items

            ]);





            setCursor(
                response.data.nextCursor
            );



            setHasMore(
                response.data.hasMore
            );



        } catch(error) {


            console.error(
                error
            );


        } finally {


            setLoadingMore(false);


        }


    }






    return {


        chats,


        loading,


        loadingMore,


        hasMore,


        loadMore,


        reload:
            loadChats


    };


}