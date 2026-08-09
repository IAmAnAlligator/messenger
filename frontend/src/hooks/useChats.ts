import {
    useCallback,
    useEffect,
    useRef,
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

    id:number;

    username:string;

    role:string;

};



export type ChatMemberDto = {

    user:UserDto;

    chatRole:string;

    joinedAt:string;

};



export type ChatDto = {

    id:number;

    name:string;

    type:"PRIVATE" | "GROUP";

    members:ChatMemberDto[];

    createdAt:string;

    lastMessageAt:string | null;

};



type ChatCursor = {

    cursorTime:string;

    cursorId:number;

};



type ChatPageResponse = {

    content:ChatDto[];

    nextCursor:ChatCursor | null;

    hasNext:boolean;

};



type ChatMemberEvent = {

    chatId:number;

    userId:number;

};



type WebSocketEvent<T> = {

    type:string;

    payload:T;

};




function mergeChats(

    oldChats:ChatDto[],

    newChats:ChatDto[]

):ChatDto[] {


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





export function useChats(){


    const { user } =
        useAuth();




    const [chats,setChats] =
        useState<ChatDto[]>([]);



    const [loading,setLoading] =
        useState(true);



    const [loadingMore,setLoadingMore] =
        useState(false);



    const [hasNext,setHasNext] =
        useState(true);



    const [cursor,setCursor] =
        useState<ChatCursor | null>(null);




    const loadingMoreRef =
        useRef(false);







    const loadChats =
        useCallback(
            async()=>{


                console.log(
                    "[loadChats] start"
                );



                try {


                    setLoading(true);



                    const response =
                        await api.get<ChatPageResponse>(
                            "/chats",
                            {
                                params:{
                                    limit:30
                                }
                            }
                        );




                    console.log(
                        "[loadChats] response",
                        {

                            size:
                                response.data.content.length,

                            ids:
                                response.data.content.map(
                                    chat =>
                                        chat.id
                                ),

                            hasNext:
                                response.data.hasNext,

                            nextCursor:
                                response.data.nextCursor

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


                }
                catch(error){


                    console.error(
                        "[loadChats] error",
                        error
                    );


                    setChats([]);


                }
                finally{


                    setLoading(false);



                    console.log(
                        "[loadChats] finished"
                    );


                }


            },
            []

        );









    const loadMore =
        useCallback(
            async()=>{


                console.log(
                    "[loadMore] called",
                    {

                        loadingMore:

                            loadingMoreRef.current,

                        stateLoadingMore:
                            loadingMore,

                        hasNext,

                        cursor

                    }
                );




                if(

                    loadingMoreRef.current ||

                    loadingMore ||

                    !hasNext ||

                    !cursor

                ){


                    console.log(
                        "[loadMore] blocked"
                    );


                    return;

                }




                try {



                    loadingMoreRef.current =
                        true;



                    setLoadingMore(true);




                    console.log(
                        "[loadMore] request",
                        {

                            cursorTime:
                                cursor.cursorTime,

                            cursorId:
                                cursor.cursorId

                        }
                    );





                    const response =
                        await api.get<ChatPageResponse>(
                            "/chats",
                            {
                                params:{

                                    cursorTime:
                                        cursor.cursorTime,

                                    cursorId:
                                        cursor.cursorId,

                                    limit:30

                                }
                            }
                        );





                    console.log(
                        "[loadMore] response",
                        {

                            size:
                                response.data.content.length,


                            ids:
                                response.data.content.map(
                                    chat =>
                                        chat.id
                                ),


                            hasNext:
                                response.data.hasNext,


                            nextCursor:
                                response.data.nextCursor

                        }
                    );





                    setChats(prev =>{


                        const merged =
                            mergeChats(
                                prev,
                                response.data.content ?? []
                            );



                        console.log(
                            "[loadMore] merged",
                            {

                                before:
                                    prev.length,

                                after:
                                    merged.length

                            }
                        );



                        return merged;

                    });





                    setCursor(
                        response.data.nextCursor
                    );



                    setHasNext(
                        response.data.hasNext
                    );



                }
                catch(error){


                    console.error(
                        "[loadMore] error",
                        error
                    );


                }
                finally{


                    loadingMoreRef.current =
                        false;



                    setLoadingMore(false);



                    console.log(
                        "[loadMore] finished"
                    );


                }


            },
            [

                cursor,

                hasNext,

                loadingMore

            ]

        );









    useEffect(()=>{


        console.log(
            "[pagination state]",
            {

                chats:
                    chats.length,

                cursor,

                hasNext,

                loadingMore

            }
        );


    },[
        chats,
        cursor,
        hasNext,
        loadingMore
    ]);








    useEffect(()=>{


        loadChats();


    },[
        loadChats
    ]);









    useEffect(()=>{


        const token =
            localStorage.getItem(
                "accessToken"
            );



        if(!token || !user){

            return;

        }




        connectSocket(token);





        subscribe(
            "/topic/chat.created",
            ()=>{

                loadChats();

            }
        );





        subscribe(
            "/topic/chat.deleted",
            message=>{


                const event =
                    JSON.parse(
                        message.body
                    );



                setChats(prev =>

                    prev.filter(
                        chat =>
                            chat.id !== event.chatId
                    )

                );


            }
        );








        const userTopic =
            `/topic/user/${user.id}/chats`;






        subscribe(
            userTopic,
            async message=>{


                const event:
                    WebSocketEvent<ChatMemberEvent> =
                    JSON.parse(
                        message.body
                    );




                switch(event.type){


                    case "CHAT_MEMBER_ADDED":{


                        try{


                            const response =
                                await api.get<ChatDto>(
                                    `/chats/${event.payload.chatId}`
                                );



                            const chat =
                                response.data;



                            setChats(prev =>

                                mergeChats(
                                    [
                                        chat,
                                        ...prev
                                    ],
                                    []
                                )

                            );


                        }
                        catch(error){

                            console.error(error);

                        }


                        break;

                    }





                    case "CHAT_MEMBER_REMOVED":{


                        setChats(prev =>

                            prev.filter(
                                chat =>
                                    chat.id !==
                                    event.payload.chatId
                            )

                        );


                        break;

                    }





                    default:

                        break;


                }


            }

        );







        return ()=>{


            unsubscribe(
                "/topic/chat.created"
            );


            unsubscribe(
                "/topic/chat.deleted"
            );


            unsubscribe(
                userTopic
            );


        };



    },[
        user,
        loadChats
    ]);







    return {


        chats,

        loading,

        loadingMore,

        hasNext,

        loadMore,

        reload:
            loadChats

    };


}