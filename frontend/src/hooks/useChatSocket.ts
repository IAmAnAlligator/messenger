import {
    useEffect,
    useState,
    useCallback
} from "react";

import {
    useNavigate
} from "react-router-dom";


import {
    useAuth
} from "../auth/AuthContext";


import {
    connectSocket,
    subscribe,
    unsubscribe,
    getSocket

} from "../services/chatSocket";


import type {
    MessageDto
} from "../services/dto/message";


import type {
    ChatSocketEvent,
    WebSocketErrorResponse

} from "../services/dto/socket";



interface Props {

    chatId?: number;

    messages: MessageDto[];

    onMessage(
        message: MessageDto
    ): void;

    onDelete(
        messageId: number
    ): void;

    onRead(
        messageId: number
    ): void;

    reloadMessages?():
        void | Promise<void>;

}





export function useChatSocket({

    chatId,

    messages,

    onMessage,

    onDelete,

    onRead,

    reloadMessages

}: Props) {


    const navigate =
        useNavigate();


    const { user } =
        useAuth();




    const [
        error,
        setError
    ] =
    useState<string | null>(null);





    /*
     * Прочитать одно сообщение
     */
    const sendRead =
        useCallback(
            (
                message: MessageDto
            ) => {


                if (!user)
                    return;



                if (
                    message.sender.id === user.id
                )
                    return;



                if (
                    message.status !== "SENT"
                )
                    return;



                const socket =
                    getSocket();



                if (!socket?.connected)
                    return;



                socket.publish({

                    destination:
                        "/app/chat.read",


                    body:
                        JSON.stringify({

                            id:
                                message.id,


                            chatId

                        })

                });


            },
            [
                chatId,
                user
            ]
        );






    /*
     * Прочитать все сообщения при открытии чата
     */
    const sendReadAll =
    useCallback(
        () => {

            console.log(
                "SEND READ ALL",
                messages
            );


            if (!user)
                return;


            const socket =
                getSocket();


            console.log(
                "SOCKET",
                socket?.connected
            );


            messages.forEach(message => {

                console.log(
                    "CHECK MESSAGE",
                    message.id,
                    message.status,
                    message.sender.id,
                    user.id
                );


                if (
                    message.sender.id === user.id
                )
                    return;


                if (
                    message.status !== "SENT"
                )
                    return;


                console.log(
                    "SEND READ",
                    message.id
                );


                socket?.publish({

                    destination:
                        "/app/chat.read",

                    body:
                        JSON.stringify({

                            id: message.id,

                            chatId

                        })

                });

            });

        },
        [
            messages,
            chatId,
            user
        ]
    );







    useEffect(() => {


        if (!chatId)
            return;



        const token =
            localStorage.getItem(
                "accessToken"
            );



        if (!token)
            return;





        const chatTopic =
            `/topic/chat/${chatId}`;



        const errorQueue =
            "/user/queue/errors";





connectSocket(
    token,
    async () => {

        sendReadAll();

        await reloadMessages?.();

    }
);


if (getSocket()?.connected) {

    sendReadAll();

}






        subscribe(
            chatTopic,
            frame => {


                const event =
                    JSON.parse(
                        frame.body
                    ) as ChatSocketEvent;




                switch(event.type) {



                    case "MESSAGE_CREATED": {


                        const message =
                            event.payload;



                        onMessage(
                            message
                        );



                        sendRead(
                            message
                        );



                        break;

                    }






                    case "MESSAGE_DELETED": {


                        onDelete(
                            event.payload.messageId
                        );


                        break;

                    }






                    case "CHAT_DELETED": {


                        if (
                            event.payload.chatId === chatId
                        ) {

                            navigate(
                                "/chats",
                                {
                                    replace:true
                                }
                            );

                        }


                        break;

                    }






                    case "MESSAGE_READ": {


                        console.log(
                            "READ EVENT",
                            event.payload
                        );



                        onRead(
                            event.payload.messageId
                        );



                        reloadMessages?.();


                        break;

                    }






                    case "CHAT_CREATED":
                    case "CHAT_RENAMED":
                    case "CHAT_MEMBER_ADDED":
                    case "CHAT_MEMBER_REMOVED":
                    case "CHAT_MEMBER_LEFT":

                        break;

                }


            }
        );






        subscribe(
            errorQueue,
            frame => {


                const response =
                    JSON.parse(
                        frame.body
                    ) as WebSocketErrorResponse;



                setError(
                    response.message
                );



                setTimeout(
                    () => {

                        setError(null);

                    },
                    4000
                );


            }
        );







        return () => {


            unsubscribe(
                chatTopic
            );


            unsubscribe(
                errorQueue
            );


        };



    }, [
        chatId,
        navigate,
        onMessage,
        onDelete,
        onRead,
        sendRead,
        sendReadAll,
        reloadMessages
    ]);







    const sendMessage =
        useCallback(
            (
                content:string
            ) => {


                const socket =
                    getSocket();



                if (
                    !socket?.connected ||
                    !chatId
                )
                    return;



                socket.publish({

                    destination:
                        "/app/chat.send",


                    body:
                        JSON.stringify({

                            chatId,

                            content

                        })

                });


            },
            [
                chatId
            ]
        );








    const deleteMessage =
        useCallback(
            (
                id:number
            ) => {


                const socket =
                    getSocket();



                if (
                    !socket?.connected ||
                    !chatId
                )
                    return;



                socket.publish({

                    destination:
                        "/app/chat.delete",


                    body:
                        JSON.stringify({

                            id,

                            chatId

                        })

                });


            },
            [
                chatId
            ]
        );







    return {

        error,

        sendMessage,

        deleteMessage

    };

}