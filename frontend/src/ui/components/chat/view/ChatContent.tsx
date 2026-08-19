import ChatHeader
    from "../list/ChatHeader";


import MessageList
    from "./MessageList";


import MessageInput
    from "./MessageInput";


import WsError
    from "../../common/WsError";



import type {
    ChatDto
} from "../../../../types/chat";


import type {
    MessageDto
} from "../../../../types/message";


import {
    useEffect
} from "react";

type Props = {


    chat: ChatDto | null;


    messages: MessageDto[];


    loading: boolean;


    loadingMore: boolean;


    hasMore: boolean;


    text: string;


    error: string | null;

    onSendFile(file: File): Promise<void>;

    onLoadMore(): void;


    onTextChange(
        value: string
    ): void;



    onSend(): void;



    onDelete(
        id: number
    ): void;



    onBack(): void;



    onEdit(): void;

    

};





export default function ChatContent(
    props: Props
) {

    
useEffect(() => {
    const page = document.querySelector(".chat-page");

    if (!page) {
        return;
    }

    let element: HTMLElement | null =
        page.parentElement;

    while (element) {

        // const rect =
        //     element.getBoundingClientRect();

        // const style =
        //     getComputedStyle(element);

         const root = document.querySelector("#root");
const app = document.querySelector(".app");

console.log({
    viewport: window.innerHeight,

    html: document.documentElement.getBoundingClientRect().height,

    body: document.body.getBoundingClientRect().height,

    root: root?.getBoundingClientRect().height,

    app: app?.getBoundingClientRect().height,

    appParent: app?.parentElement,

    rootChild: root?.firstElementChild
});

        // console.log(
        //     "PARENT",
        //     element.className || element.tagName,
        //     {
                
        //         height: rect.height,
        //         width: rect.width,
        //         top: rect.top,
        //         bottom: rect.bottom,

        //         display: style.display,
        //         position: style.position,

        //         flex:
        //             style.flex,

        //         flexDirection:
        //             style.flexDirection,

        //         minHeight:
        //             style.minHeight,

        //         heightCss:
        //             style.height,

        //         overflow:
        //             style.overflow
        //     }
        // );

        element =
            element.parentElement;
    }

}, []);

    return (

        <div className="chat-page">


            <ChatHeader

                chat={props.chat}

                onBack={props.onBack}

                onEdit={props.onEdit}

            />



            <WsError

                message={props.error}

            />



            <MessageList


                loading={props.loading}


                loadingMore={props.loadingMore}


                hasMore={props.hasMore}


                messages={props.messages}


                onLoadMore={props.onLoadMore}


                onDelete={props.onDelete}


            />



            <MessageInput


                value={props.text}


                onChange={props.onTextChange}


                onSend={props.onSend}

                onSendFile={props.onSendFile}


            />


        </div>

    );

}