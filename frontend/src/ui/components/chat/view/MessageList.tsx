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



    const previousCount =
        useRef(0);





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



            previousCount.current =
                messages.length;

        }


    }, [messages]);





    /*
        Новое сообщение:
        двигаем вниз
    */
    useEffect(() => {


        if (
            initialized.current &&
            messages.length >
                previousCount.current
        ) {


            bottomRef.current?.scrollIntoView({

                behavior: "smooth"

            });


        }



        previousCount.current =
            messages.length;



    }, [messages]);







    /*
        Загрузка старых сообщений
        при прокрутке вверх
    */
    useEffect(() => {


        const container =
            containerRef.current;



        if (!container) {

            return;

        }



        const handleScroll = () => {


            if (

                container.scrollTop <= 100 &&

                hasMore &&

                !loadingMore

            ) {


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

        onLoadMore

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
                loadingMore && (

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