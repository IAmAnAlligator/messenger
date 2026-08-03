import {
    useEffect,
    useState
} from "react";

import { api } from "../api/client";


export type ChatType =
    | "PRIVATE"
    | "GROUP";


export type UserDto = {

    id:number;

    username:string;

    role:string;

};



export function useChatCreate(){


    const [type,setType]
        = useState<ChatType>("GROUP");


    const [name,setName]
        = useState("");


    const [search,setSearch]
        = useState("");


    const [users,setUsers]
        = useState<UserDto[]>([]);


    const [selectedUsers,setSelectedUsers]
        = useState<UserDto[]>([]);


    const [searchLoading,setSearchLoading]
        = useState(false);



    useEffect(()=>{


        const timer =
            setTimeout(()=>{


                const q =
                    search.trim();


                if(q){

                    searchUsers(q);

                }
                else{

                    setUsers([]);

                }


            },300);



        return ()=>clearTimeout(timer);


    },[search]);




    async function searchUsers(
        query:string
    ){

        try{

            setSearchLoading(true);


            const res =
                await api.get(
                    "/users/search",
                    {
                        params:{
                            query
                        }
                    }
                );


            setUsers(
                Array.isArray(res.data)
                ? res.data
                : []
            );


        }
        finally{

            setSearchLoading(false);

        }

    }




    function addUser(
        user:UserDto
    ){

        setSelectedUsers(prev=>{


            if(type==="PRIVATE"){

                return [
                    user
                ];

            }


            if(
                prev.some(
                    u=>u.id===user.id
                )
            ){

                return prev;

            }


            return [
                ...prev,
                user
            ];

        });


        setSearch("");

        setUsers([]);

    }




    function removeUser(
        id:number
    ){

        setSelectedUsers(prev=>
            prev.filter(
                u=>u.id!==id
            )
        );

    }




    async function createChat(){


        await api.post(
            "/chats",
            {

                type,

                name:
                    type==="GROUP"
                    ? name
                    : null,


                memberIds:
                    selectedUsers.map(
                        u=>u.id
                    )

            }
        );

    }




    return {

        type,
        setType,

        name,
        setName,

        search,
        setSearch,

        users,

        selectedUsers,

        searchLoading,

        addUser,

        removeUser,

        createChat

    };

}