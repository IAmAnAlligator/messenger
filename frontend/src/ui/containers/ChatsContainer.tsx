import {
    useNavigate
} from "react-router-dom";


import {
    useAuth
} from "../../contexts/AuthContext";


import {
    useChats
} from "../../hooks/useChats";


import ChatsHeader
from "../components/chat/view/ChatsHeader";


import ChatList
from "../components/chat/list/ChatList";



import "../styles/chatsPage.css";



export default function ChatsPage(){


    const navigate =
        useNavigate();



    const {
        user,
        logout
    } = useAuth();



    const {
        chats,
        loading
    } =
    useChats();






    async function handleLogout(){

        await logout();


        navigate(
            "/",
            {
                replace:true
            }
        );

    }






    function getChatName(chat:any){


        if(chat.type === "GROUP")
            return chat.name;



        const other =
            chat.members.find(
                (m:any)=>
                    m.user.id !== user?.id
            );



        return (
            other?.user.username
            ??
            chat.name
        );

    }







return (

<div className="chats-page">


    <ChatsHeader

        onLogout={
            handleLogout
        }

    />



    <button
        className="create-chat-btn"
        onClick={() =>
            navigate(
                "/chats/create"
            )
        }
    >
        Create chat
    </button>




    {
        loading &&
        <p>
            Loading...
        </p>
    }



    {
        !loading &&
        chats.length === 0 &&
        <p>
            No chats
        </p>
    }



    <ChatList

        chats={chats}

        getName={getChatName}

        onOpen={
            id =>
                navigate(
                    `/chats/${id}`
                )
        }

    />



</div>

);


}