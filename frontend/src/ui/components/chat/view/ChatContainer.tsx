import {
    useState
} from "react";

import {
    useNavigate
} from "react-router-dom";


import {
    useChat
} from "../../../../hooks/useChat";


import {
    useChatSocket
} from "../../../../hooks/useChatSocket";


import ChatContent
    from "./ChatContent";



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

        loadMoreMessages


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







    return (

        <ChatContent

            chat={chat}

            messages={messages}

            loading={loading}

            loadingMore={loadingMore}

            hasMore={hasMore}

            onLoadMore={loadMoreMessages}


            text={text}

            error={error}



            onTextChange={setText}



            onSend={() => {


                const content =
                    text.trim();



                if (!content) {
                    return;
                }



                sendMessage(
                    content
                );



                setText("");

            }}



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