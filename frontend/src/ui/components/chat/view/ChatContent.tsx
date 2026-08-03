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



type Props = {


    chat: ChatDto | null;


    messages: MessageDto[];


    loading: boolean;


    loadingMore: boolean;


    hasMore: boolean;


    text: string;


    error: string | null;



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


            />


        </div>

    );

}