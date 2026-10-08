import {
    useNavigate
} from "react-router-dom";


import {
    useAuth
} from "../../contexts/AuthContext";


import {
    useChats
} from "../../hooks/useChats";


import type {
    ChatDto
} from "../../hooks/useChats";


import ChatsHeader
    from "../components/chat/view/ChatsHeader";


import ChatList
    from "../components/chat/list/ChatList";


import "../styles/chatsPage.css";


export default function ChatsContainer() {

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


    function handleLogout() {

        logout();

        navigate("/");

    }


    function handleProfile() {

        navigate("/profile");

    }


    function getChatName(
        chat: ChatDto
    ): string {

        if (
            chat.type === "GROUP"
        ) {

            return chat.name;

        }


        const otherMember =
            chat.members.find(
                member =>
                    member.user.id !==
                    user?.id
            );


        return (
            otherMember?.user.username ??
            chat.name
        );

    }


    function handleOpenChat(
        chatId: string
    ) {

        navigate(
            `/chats/${chatId}`
        );

    }


    function handleNewGroup() {

        navigate(
            "/chats/create/group"
        );

    }


    function handleNewPrivate() {

        navigate(
            "/chats/create/private"
        );

    }


    return (

        <div className="chats-container">

            <ChatsHeader

                onLogout={
                    handleLogout
                }

                onProfile={
                    handleProfile
                }

            />


            <div className="chats-actions">

                <button
                    onClick={
                        handleNewGroup
                    }
                >
                    New group
                </button>


                <button
                    onClick={
                        handleNewPrivate
                    }
                >
                    New chat
                </button>

            </div>


            {loading && (

                <div className="chat-loader">

                    Loading...

                </div>

            )}


            {!loading &&
                chats.length === 0 && (

                    <div className="chat-empty">

                        No chats

                    </div>

                )}


            {!loading &&
                chats.length > 0 && (

                    <ChatList

                        chats={chats}

                        getName={
                            getChatName
                        }

                        onOpen={
                            handleOpenChat
                        }

                        hasNext={
                            hasNext
                        }

                        loadingMore={
                            loadingMore
                        }

                        loadMore={
                            loadMore
                        }

                    />

                )}

        </div>

    );

}
