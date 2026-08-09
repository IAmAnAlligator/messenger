import {
    useEffect,
    useRef
} from "react";


import MessageItem
    from "./MessageItem";


import type {
    MessageDto
} from "../../../../types/message";


import "../../../styles/chatPage.css";



type Props = {

    loading: boolean;

    loadingMore: boolean;

    hasMore: boolean;

    messages: MessageDto[];

    onDelete(id: number): void;

    onLoadMore(): void;

};



export default function MessageList({

    loading,

    loadingMore,

    hasMore,

    messages,

    onDelete,

    onLoadMore

}: Props) {


    const containerRef =
        useRef<HTMLDivElement>(null);



    const bottomRef =
        useRef<HTMLDivElement>(null);



    const initialized =
        useRef(false);



    const previousLastMessageId =
        useRef<number | null>(null);



    const previousScrollHeight =
        useRef(0);



    const previousMessagesLength =
        useRef(0);



    const loadingMoreRef =
        useRef(false);





    /*
        Первый вход в чат:
        показываем последнее сообщение
    */
    useEffect(() => {


        if (
            !initialized.current &&
            messages.length > 0
        ) {


            bottomRef.current?.scrollIntoView();



            initialized.current = true;



            previousLastMessageId.current =
                messages[
                    messages.length - 1
                ].id;

        }


    }, [messages]);







    /*
        Новое сообщение:
        прокручиваем вниз.
        Старые сообщения не трогаем.
    */
    useEffect(() => {


        if (!initialized.current) {
            return;
        }



        const lastMessage =
            messages[
                messages.length - 1
            ];



        if (

            lastMessage &&

            previousLastMessageId.current !== null &&

            lastMessage.id !==
                previousLastMessageId.current &&

            !loadingMoreRef.current

        ) {


            bottomRef.current?.scrollIntoView({

                behavior: "smooth"

            });

        }



        previousLastMessageId.current =
            lastMessage?.id ?? null;



    }, [messages]);







    /*
        Восстановление позиции после загрузки
        старых сообщений
    */
    useEffect(() => {


        const container =
            containerRef.current;



        if (
            !container ||
            !loadingMoreRef.current
        ) {
            return;
        }



        const addedMessages =
            messages.length >
            previousMessagesLength.current;



        if (addedMessages) {


            const newScrollHeight =
                container.scrollHeight;



            requestAnimationFrame(() => {


                container.scrollTop =
                    newScrollHeight -
                    previousScrollHeight.current;



                loadingMoreRef.current =
                    false;


            });


        } else {


            loadingMoreRef.current =
                false;

        }



    }, [messages]);







    /*
        Загрузка старых сообщений
    */
    useEffect(() => {


        const container =
            containerRef.current;



        if (!container) {
            return;
        }



        const handleScroll = () => {


            if (

                container.scrollTop <= 20 &&

                hasMore &&

                !loadingMore &&

                messages.length > 0

            ) {


                previousScrollHeight.current =
                    container.scrollHeight;



                previousMessagesLength.current =
                    messages.length;



                loadingMoreRef.current =
                    true;



                onLoadMore();

            }


        };



        container.addEventListener(

            "scroll",

            handleScroll

        );



        return () =>

            container.removeEventListener(

                "scroll",

                handleScroll

            );



    }, [

        hasMore,

        loadingMore,

        onLoadMore,

        messages.length

    ]);







    if (loading) {


        return (

            <div className="messages">

                Loading...

            </div>

        );

    }






    return (

        <div

            className="messages"

            ref={containerRef}

        >



            {
                loadingMore && hasMore && (

                    <div className="messages-loader">

                        Loading...

                    </div>

                )
            }




            {
                messages.map(message => (

                    <MessageItem

                        key={message.id}

                        message={message}

                        onDelete={onDelete}

                    />

                ))
            }




            <div ref={bottomRef} />



        </div>

    );

}