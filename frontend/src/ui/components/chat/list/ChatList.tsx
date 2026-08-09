import {
    useEffect,
    useRef
} from "react";


import ChatItem from "./ChatItem";


import type {
    ChatDto
} from "../../../../hooks/useChats";



type Props = {

    chats: ChatDto[];

    getName(chat:ChatDto):string;

    onOpen(id:number):void;

    hasNext:boolean;

    loadingMore:boolean;

    loadMore():void;

};





export default function ChatList({

    chats,

    getName,

    onOpen,

    hasNext,

    loadingMore,

    loadMore

}:Props){



    const containerRef =
        useRef<HTMLDivElement>(null);




 useEffect(() => {
    const container = containerRef.current;

    if (!container) {
        return;
    }

    const handleScroll = () => {
        const distanceFromBottom =
            container.scrollHeight -
            container.scrollTop -
            container.clientHeight;

        if (
            distanceFromBottom <= 100 &&
            hasNext &&
            !loadingMore
        ) {
            loadMore();
        }
    };

    container.addEventListener("scroll", handleScroll);

    handleScroll();

    return () => {
        container.removeEventListener(
            "scroll",
            handleScroll
        );
    };
}, [
    hasNext,
    loadingMore,
    loadMore,
    chats.length
]);







    return (

        <div
            className="chat-content"
        >


            <div

                className="chat-list-container"

                ref={containerRef}

            >


                <div className="chat-list">


                    {
                        chats.map(chat=>(


                            <ChatItem

                                key={chat.id}

                                chat={chat}

                                name={
                                    getName(chat)
                                }

                                onClick={()=>
                                    onOpen(chat.id)
                                }

                            />


                        ))
                    }



                    {
                        loadingMore && (

                            <div className="chat-loader">

                                Loading...

                            </div>

                        )
                    }


                </div>


            </div>


        </div>

    );

}