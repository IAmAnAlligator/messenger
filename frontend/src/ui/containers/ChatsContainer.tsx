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
        loading,
        loadingMore,
        hasNext,
        loadMore

    } = useChats();




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

        if(chat.type === "GROUP") {
            return chat.name;
        }


        const other =
            chat.members.find(
                (m:any) =>
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




            <div className="chat-content">


                {
                    loading && (

                        <div className="chat-loading">

                            Loading...

                        </div>

                    )
                }



                {
                    !loading &&
                    chats.length === 0 && (

                        <div className="empty-chats">

                            No chats

                        </div>

                    )
                }




                {
                    !loading &&
                    chats.length > 0 && (

                        <ChatList

                            chats={chats}

                            getName={getChatName}

                            onOpen={
                                id =>
                                    navigate(
                                        `/chats/${id}`
                                    )
                            }

                            hasNext={hasNext}

                            loadingMore={loadingMore}

                            loadMore={loadMore}

                        />

                    )
                }
                


            </div>


        </div>

    );

}