import {
    useEffect,
    useState
} from "react";


import {
    getChat
} from "../services/chatService";


import {
    getMessages
} from "../services/messageService";


import type {
    ChatDto
} from "../types/chat";


import type {
    MessageDto
} from "../types/message";



export function useChat(
    chatId?: number
) {


    const [chat, setChat] =
        useState<ChatDto | null>(null);



    const [messages, setMessages] =
        useState<MessageDto[]>([]);



    const [loading, setLoading] =
        useState(true);



    const [loadingMore, setLoadingMore] =
        useState(false);



    const [cursor, setCursor] =
        useState<number | null>(null);



    const [hasMore, setHasMore] =
        useState(true);




    useEffect(() => {

        if (!chatId) {
            return;
        }


        load();


    }, [chatId]);




    async function load() {

        if (!chatId) {
            return;
        }


        try {


            setLoading(true);



            const [
                chatData,
                messagesPage

            ] = await Promise.all([


                getChat(
                    chatId
                ),


                getMessages(
                    chatId
                )

            ]);



            setChat(
                chatData
            );



            setMessages(
                messagesPage.items
                    .reverse()
            );



            setCursor(
                messagesPage.nextCursor
            );



            setHasMore(
                messagesPage.hasMore
            );



        } finally {

            setLoading(false);

        }

    }




    async function loadMoreMessages() {


        if (
            !chatId ||
            !hasMore ||
            loadingMore ||
            !cursor
        ) {
            return;
        }



        try {


            setLoadingMore(true);



            const page =
                await getMessages(
                    chatId,
                    cursor
                );



            setMessages(prev => [

                ...page.items.reverse(),

                ...prev

            ]);



            setCursor(
                page.nextCursor
            );



            setHasMore(
                page.hasMore
            );



        } catch(error) {


            console.error(
                "Failed to load more messages",
                error
            );


        } finally {


            setLoadingMore(false);

        }

    }





    async function reloadMessages() {


        if (!chatId) {
            return;
        }



        try {


            const page =
                await getMessages(
                    chatId
                );



            setMessages(
                page.items.reverse()
            );



            setCursor(
                page.nextCursor
            );



            setHasMore(
                page.hasMore
            );



        } catch(error) {


            console.error(
                "Failed to reload messages",
                error
            );

        }

    }






    function addMessage(
        message: MessageDto
    ) {


        setMessages(prev => {


            const exists =
                prev.some(
                    m =>
                        m.id === message.id
                );



            if (exists) {

                return prev;

            }



            return [

                ...prev,

                message

            ];

        });


    }






    function removeMessage(
        id:number
    ) {


        setMessages(prev =>

            prev.filter(
                m =>
                    m.id !== id
            )

        );

    }






    function updateMessageStatus(
        messageId:number
    ) {


        setMessages(prev =>

            prev.map(message => {


                if (
                    message.id === messageId
                ) {


                    return {

                        ...message,

                        status:"READ"

                    };

                }


                return message;


            })

        );

    }





    return {


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


    };

}