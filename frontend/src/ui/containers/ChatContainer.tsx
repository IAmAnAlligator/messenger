import {
    useState
} from "react";


import {
    useNavigate
} from "react-router-dom";


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



    const [text, setText] =
        useState("");



    const {

        chat,

        messages,

        loading,

        loadingMore,

        hasMore,

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

        deleteMessage

    } = useChatSocket({

        chatId,

        messages,

        onMessage: addMessage,

        onDelete: removeMessage,

        onRead: updateMessageStatus,

        reloadMessages

    });




    async function handleSendFile(
        file: File
    ) {

        try {

            await sendFile(file);

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



    return (

        <ChatContent

            chat={chat}

            messages={messages}

            loading={loading}

            loadingMore={loadingMore}

            hasMore={hasMore}


            onLoadMore={
                loadMoreMessages
            }


            onSendFile={
                handleSendFile
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