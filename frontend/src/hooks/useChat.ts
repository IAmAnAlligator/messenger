import { useEffect, useState } from "react";

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

            const [
                chatData,
                messagesData

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
                messagesData
            );


        } finally {

            setLoading(false);

        }

    }




    async function reloadMessages() {

        if (!chatId) {
            return;
        }


        try {

            const messagesData =
                await getMessages(
                    chatId
                );


                        console.log(
            "RELOADED MESSAGES",
            messagesData.find(
                m => m.id === 39
            )
        );


            setMessages(
                messagesData
            );


        } catch (error) {

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
        id: number
    ) {

        setMessages(prev =>
            prev.filter(
                m =>
                    m.id !== id
            )
        );

    }





    function updateMessageStatus(
        messageId: number
    ) {

        setMessages(prev =>
            prev.map(message => {

                if (message.id === messageId) {

                    return {
                        ...message,
                        status: "READ"
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

        addMessage,

        removeMessage,

        updateMessageStatus,

        reloadMessages

    };

}