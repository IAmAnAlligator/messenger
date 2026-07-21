import {
    useEffect,
    useMemo,
    useState
} from "react";

import {
    useNavigate,
    useParams
} from "react-router-dom";

import { api } from "../api/client";

import {
    connectSocket,
    subscribe,
    unsubscribe
} from "../websocket/chatSocket";


type UserDto = {
    id: number;
    username: string;
    role: string;
};


type ChatMemberDto = {
    user: UserDto;
    chatRole: "ADMIN" | "MEMBER";
    joinedAt: string;
};


type ChatDto = {
    id: number;
    name?: string;
    type: "PRIVATE" | "GROUP";
};



export default function ChatEditPage() {

    const { chatId } = useParams();

    const navigate = useNavigate();


    const [chat, setChat] =
        useState<ChatDto | null>(null);


    const [members, setMembers] =
        useState<ChatMemberDto[]>([]);


    const [chatName, setChatName] =
        useState("");


    const [searchUsername, setSearchUsername] =
        useState("");


    const [users, setUsers] =
        useState<UserDto[]>([]);


    const [searchLoading, setSearchLoading] =
        useState(false);


    const [loading, setLoading] =
        useState(true);


    const [currentUserId, setCurrentUserId] =
        useState<number | null>(null);



    useEffect(() => {

        if (!chatId) {
            return;
        }


        const token =
            localStorage.getItem("accessToken");


        if (token) {
            connectSocket(token);
        }


        load();


        const destination =
            `/topic/chat/${chatId}`;


        subscribe(destination, frame => {

            const incoming =
                JSON.parse(frame.body);


            if (
                incoming?.type === "CHAT_DELETED" ||
                incoming?.event === "CHAT_DELETED"
            ) {

                redirectToChats();

            }

        });


        return () => {

            unsubscribe(destination);

        };


    }, [chatId]);




    async function load() {

        try {

            setLoading(true);


            const [
                chatResponse,
                membersResponse,
                meResponse
            ] = await Promise.all([

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


        } catch (e:any) {

            if (
                e.response?.status === 404
            ) {

                redirectToChats();

                return;

            }


            console.error(e);


        } finally {

            setLoading(false);

        }

    }




    function redirectToChats() {

        navigate(
            "/chats",
            {
                replace:true
            }
        );

    }




    const currentMember =
        useMemo(
            () =>
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



    const canDeleteChat =
        chat?.type === "PRIVATE" ||
        (
            chat?.type === "GROUP" &&
            isAdmin
        );



    const canRemoveMembers =
        chat?.type === "GROUP" &&
        isAdmin;



    const canAddMembers =
        chat?.type === "GROUP" &&
        isAdmin;



    const canRenameChat =
        chat?.type === "GROUP" &&
        isAdmin;



    const canLeaveChat =
        chat?.type === "GROUP" &&
        currentMember?.chatRole === "MEMBER";




    useEffect(() => {

        const timer =
            setTimeout(() => {

                const query =
                    searchUsername.trim();


                if (
                    query &&
                    canAddMembers
                ) {

                    searchUsers();

                } else {

                    setUsers([]);

                }


            }, 300);



        return () =>
            clearTimeout(timer);


    }, [
        searchUsername,
        canAddMembers
    ]);

        async function renameChat() {

        const name =
            chatName.trim();


        if (!name) {

            alert(
                "Chat name cannot be empty"
            );

            return;
        }


        try {

            await api.patch(
                `/chats/${chatId}/name`,
                {
                    name
                }
            );


            setChat(prev =>
                prev
                    ? {
                        ...prev,
                        name
                    }
                    : prev
            );


        } catch (e) {

            console.error(e);

            alert(
                "Cannot rename chat"
            );

        }

    }





    async function searchUsers() {

        const query =
            searchUsername.trim();


        if (!query) {

            setUsers([]);

            return;

        }


        try {

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


            const result =
                Array.isArray(res.data)
                    ? res.data
                    : [];


            setUsers(
                result.filter(
                    (user: UserDto) =>
                        !members.some(
                            member =>
                                member.user.id === user.id
                        )
                )
            );


        } catch (e) {

            console.error(e);


        } finally {

            setSearchLoading(false);

        }

    }





    async function addMember(user: UserDto) {

        try {

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


        } catch(e) {

            console.error(e);

            alert(
                "Cannot add member"
            );

        }

    }





    async function removeMember(userId:number) {

        try {

            await api.delete(
                `/chats/${chatId}/members/${userId}`
            );


            await load();


        } catch(e) {

            console.error(e);

        }

    }





    async function leaveChat() {

        if(
            !window.confirm(
                "Leave this chat?"
            )
        ){
            return;
        }


        try {

            await api.delete(
                `/chats/${chatId}/leave`
            );


            navigate(
                "/chats",
                {
                    replace:true
                }
            );


        } catch(e){

            console.error(e);

        }

    }





    async function deleteChat(){

        if(
            !window.confirm(
                "Delete this chat?"
            )
        ){
            return;
        }


        try{

            await api.delete(
                `/chats/${chatId}`
            );


            redirectToChats();


        }catch(e){

            console.error(e);

        }

    }





    return (

        <div
            style={{
                padding:20
            }}
        >


            <div
                style={{
                    display:"flex",
                    justifyContent:"space-between",
                    alignItems:"center",
                    marginBottom:20
                }}
            >

                <button
                    onClick={() =>
                        navigate(`/chats/${chatId}`)
                    }
                >
                    ← Back
                </button>



                <h2>
                    {
                        chat?.type === "PRIVATE"
                            ? "Private Chat"
                            : "Group Settings"
                    }
                </h2>



                <div>

                    {
                        canLeaveChat && (

                            <button
                                onClick={leaveChat}
                            >
                                Leave
                            </button>

                        )
                    }


                    {
                        canDeleteChat && (

                            <button
                                onClick={deleteChat}
                                style={{
                                    marginLeft:10,
                                    background:"#d33",
                                    color:"white"
                                }}
                            >
                                Delete
                            </button>

                        )
                    }


                </div>


            </div>





            {
                canRenameChat && (

                    <div
                        style={{
                            display:"flex",
                            gap:10,
                            marginBottom:20
                        }}
                    >

                        <input
                            value={chatName}
                            onChange={
                                e =>
                                    setChatName(
                                        e.target.value
                                    )
                            }
                            placeholder="Group name"
                        />


                        <button
                            onClick={renameChat}
                        >
                            Rename
                        </button>

                    </div>

                )
            }






            {
                canAddMembers && (

                    <div
                        style={{
                            border:"1px solid #ddd",
                            padding:15,
                            marginBottom:20
                        }}
                    >

                        <h3>
                            Add member
                        </h3>



                        <input
                            value={searchUsername}
                            placeholder="Search users"
                            onChange={
                                e =>
                                    setSearchUsername(
                                        e.target.value
                                    )
                            }
                        />



                        {
                            searchLoading && (
                                <p>
                                    Searching...
                                </p>
                            )
                        }



                        {
                            users.map(user => (

                                <div
                                    key={user.id}
                                    onClick={() =>
                                        addMember(user)
                                    }
                                    style={{
                                        cursor:"pointer",
                                        padding:8,
                                        borderBottom:
                                            "1px solid #ddd"
                                    }}
                                >

                                    {user.username}

                                </div>

                            ))
                        }


                    </div>

                )
            }






            {
                loading && (
                    <p>
                        Loading...
                    </p>
                )
            }





            {
                members.map(member => {


                    const canRemove =
                        canRemoveMembers &&
                        member.chatRole !== "ADMIN" &&
                        member.user.id !== currentUserId;



                    return (

                        <div
                            key={member.user.id}
                            style={{
                                display:"flex",
                                justifyContent:"space-between",
                                alignItems:"center",
                                padding:14,
                                border:"1px solid #ddd",
                                marginBottom:10
                            }}
                        >

                            <div>

                                <b>
                                    {member.user.username}
                                </b>


                                <div>
                                    Role: {member.chatRole}
                                </div>


                            </div>



                            {
                                canRemove && (

                                    <button
                                        onClick={() =>
                                            removeMember(
                                                member.user.id
                                            )
                                        }
                                    >
                                        Remove
                                    </button>

                                )
                            }


                        </div>

                    );


                })
            }


        </div>

    );

}