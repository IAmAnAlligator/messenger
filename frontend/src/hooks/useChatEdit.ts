import {
    useCallback,
    useEffect,
    useMemo,
    useState
} from "react";

import { useNavigate } from "react-router-dom";

import { api } from "../api/client";

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

    chatRole:"ADMIN"|"MEMBER";

    joinedAt:string;

};


export type ChatDto = {

    id:number;

    name?:string;

    type:"PRIVATE"|"GROUP";

};




export function useChatEdit(
    chatId?:string
){


    const navigate =
        useNavigate();



    const [chat,setChat] =
        useState<ChatDto|null>(null);



    const [members,setMembers] =
        useState<ChatMemberDto[]>([]);



    const [chatName,setChatName] =
        useState("");



    const [users,setUsers] =
        useState<UserDto[]>([]);



    const [searchUsername,setSearchUsername] =
        useState("");



    const [searchLoading,setSearchLoading] =
        useState(false);



    const [loading,setLoading] =
        useState(true);



    const [currentUserId,setCurrentUserId] =
        useState<number|null>(null);



       const load = useCallback(async () => {


        if(!chatId)
            return;



        try{


            setLoading(true);



            const [

                chatResponse,

                membersResponse,

                meResponse


            ] =
            await Promise.all([


                api.get(
                    `/chats/${chatId}`
                ),


                api.get(
                    `/chats/${chatId}/members`
                ),


                api.get(
                    "/users/me"
                )


            ]);




            setChat(
                chatResponse.data
            );



            setChatName(
                chatResponse.data.name ?? ""
            );



            setMembers(
                Array.isArray(
                    membersResponse.data
                )
                    ? membersResponse.data
                    : []
            );



            setCurrentUserId(
                meResponse.data.id
            );



        }
        catch(error){

            console.error(
                "LOAD CHAT ERROR",
                error
            );

        }
        finally{

            setLoading(false);

        }


    }, [chatId]);






    useEffect(()=>{


        if(!chatId)
            return;



        const token =
            localStorage.getItem(
                "accessToken"
            );


        if(token)
            connectSocket(token);



        load();



        const topic =
            `/topic/chat/${chatId}`;



subscribe(
    topic,
    async frame => {

        const event =
            JSON.parse(
                frame.body
            );

        switch (event.type) {

            case "CHAT_DELETED":

                navigate(
                    "/chats",
                    {
                        replace: true
                    }
                );

                break;


case "CHAT_MEMBER_REMOVED": {

    if (
        event.payload.chatId !== Number(chatId)
    ) {
        break;
    }

    if (
        currentUserId !== null &&
        event.payload.userId === currentUserId
    ) {

        navigate(
            "/chats",
            {
                replace: true
            }
        );

        return;
    }

    await load();

    break;
}

case "CHAT_MEMBER_ADDED":
case "CHAT_MEMBER_LEFT":
case "CHAT_RENAMED":

    await load();

    break;

            

            default:
                break;
        }

    }
);



        return ()=>{

            unsubscribe(topic);

        };


    }, [
    load,
    navigate,
    currentUserId
]);








    /*
        Автоматический поиск пользователей
    */
    useEffect(()=>{


        const timer =
            setTimeout(()=>{


                const query =
                    searchUsername.trim();



                if(
                    query.length > 0
                ){

                    searchUsers();


                } else {


                    setUsers([]);

                }



            },300);



        return ()=>{

            clearTimeout(timer);

        };


    },[
        searchUsername,
        members
    ]);



    const currentMember =
        useMemo(
            ()=>


                members.find(
                    member =>
                        member.user.id === currentUserId
                ),


            [
                members,
                currentUserId
            ]

        );





    const isAdmin =
        currentMember?.chatRole === "ADMIN";





    const permissions = {


        canRename:

            chat?.type === "GROUP"

            &&

            isAdmin,



        canAdd:

            chat?.type === "GROUP"

            &&

            isAdmin,



        canRemove:

            chat?.type === "GROUP"

            &&

            isAdmin,



        canDelete:

            chat?.type === "PRIVATE"

            ||

            (
                chat?.type === "GROUP"
                &&
                isAdmin
            ),



        canLeave:

            chat?.type === "GROUP"

            &&

            currentMember?.chatRole === "MEMBER"


    };









    async function renameChat(){


        const name =
            chatName.trim();



        if(!name)
            return;



        try{


            await api.patch(

                `/chats/${chatId}/name`,

                {
                    name
                }

            );


            await load();


        }
        catch(error){

            console.error(
                "RENAME ERROR",
                error
            );

        }


    }









    async function searchUsers(){


        try{


            setSearchLoading(true);



            const response =
                await api.get(
                    "/users/search",
                    {
                        params:{
                            query:
                                searchUsername.trim()
                        }
                    }
                );



            const result =
                Array.isArray(
                    response.data
                )
                    ? response.data
                    : [];




            setUsers(

                result.filter(
                    (user:UserDto)=>

                        !members.some(
                            member =>
                                member.user.id === user.id
                        )
                )

            );


        }
        catch(error){

            console.error(
                "SEARCH USERS ERROR",
                error
            );


            setUsers([]);

        }
        finally{

            setSearchLoading(false);

        }


    }









    async function addMember(
        user:UserDto
    ){


        try{


            await api.post(

                `/chats/${chatId}/members`,

                null,

                {
                    params:{
                        userId:user.id
                    }
                }

            );



            await load();



            setSearchUsername("");

            setUsers([]);



        }
        catch(error){

            console.error(
                "ADD MEMBER ERROR",
                error
            );

        }


    }









    async function removeMember(
        userId:number
    ){


        try{


            await api.delete(

                `/chats/${chatId}/members/${userId}`

            );


            await load();


        }
        catch(error){

            console.error(
                error
            );

        }


    }









    async function leaveChat(){


        await api.delete(

            `/chats/${chatId}/leave`

        );



        navigate(
            "/chats"
        );


    }









    async function deleteChat(){


        await api.delete(

            `/chats/${chatId}`

        );



        navigate(
            "/chats"
        );


    }









    return {


        chat,


        members,


        loading,



        chatName,

        setChatName,



        searchUsername,

        setSearchUsername,



        users,


        searchLoading,



        currentUserId,



        permissions,



        searchUsers,



        renameChat,



        addMember,



        removeMember,



        leaveChat,



        deleteChat


    };


}