import { useEffect, useState } from "react";

import { api } from "../services/client";

import {
    connectSocket,
    subscribe,
    unsubscribe
} from "../services/chatSocket";


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

};



export function useChats() {


    const [chats,setChats] =
        useState<ChatDto[]>([]);


    const [loading,setLoading] =
        useState(true);




    useEffect(() => {

        loadChats();

    }, []);





    useEffect(() => {


        const token =
            localStorage.getItem(
                "accessToken"
            );


        if(!token)
            return;



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



        return () => {


            unsubscribe(
                "/topic/chat.created"
            );


            unsubscribe(
                "/topic/chat.deleted"
            );

        };


    }, []);





    async function loadChats(){

        try{

            setLoading(true);


            const response =
                await api.get(
                    "/chats"
                );


            setChats(
                response.data
            );


        }
        catch(error){

            console.error(error);

        }
        finally{

            setLoading(false);

        }

    }





    return {

        chats,

        loading,

        reload:loadChats

    };

}