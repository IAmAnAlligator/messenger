import {
    useState
} from "react";

import {
    useNavigate
} from "react-router-dom";

import {
    useAuth
} from "../../contexts/AuthContext";

import {
    useChat
} from "../../hooks/useChat";

import {
    useChatSocket
} from "../../hooks/useChatSocket";

import ChatContent
    from "../components/chat/view/ChatContent";


type Props = {

    chatId?: number;

};


export default function ChatContainer({
    chatId
}: Props) {

    const navigate =
        useNavigate();

    const { user } =
        useAuth();


    const [text, setText] =
        useState("");


    const {

        chat,

        messages,

        loading,

        loadingMore,

        hasMore,

        otherUserLastReadMessageId,

        addMessage,

        removeMessage,

        updateMessageStatus,

        reloadMessages,

        loadMoreMessages,

        sendFile

    } = useChat(chatId);


    const {

        error,

        sendMessage,

        deleteMessage,

        sendReadUpTo

    } = useChatSocket({

        chatId,

        onMessage:
            addMessage,

        onDelete:
            removeMessage,

        onRead:
            updateMessageStatus,

        reloadMessages

    });


    async function handleSendFile(
        file: File
    ) {

        try {

            await sendFile(
                file
            );

        } catch (error) {

            console.error(
                "Failed to send file",
                error
            );

        }

    }


    function handleSend() {

        const content =
            text.trim();


        if (!content) {
            return;
        }


        sendMessage(
            content
        );


        setText("");

    }


    if (!user) {
        return null;
    }


    return (

        <ChatContent

            chat={chat}

            messages={messages}

            loading={loading}

            loadingMore={loadingMore}

            hasMore={hasMore}

            currentUserId={
                user.id
            }

            lastReadMessageId={
                otherUserLastReadMessageId
            }

            onLoadMore={
                loadMoreMessages
            }

            onSendFile={
                handleSendFile
            }

            onReadUpTo={
                sendReadUpTo
            }

            text={text}

            error={error}

            onTextChange={
                setText
            }

            onSend={
                handleSend
            }

            onDelete={
                deleteMessage
            }

            onBack={() =>
                navigate(
                    "/chats"
                )
            }

            onEdit={() =>
                navigate(
                    `/chats/${chatId}/edit`
                )
            }

        />

    );

}