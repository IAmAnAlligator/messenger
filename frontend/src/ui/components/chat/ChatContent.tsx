import ChatHeader from "./ChatHeader";
import MessageList from "./MessageList";
import MessageInput from "./MessageInput";
import WsError from "./WsError";

import type { ChatDto } from "../../../services/dto/chat";
import type { MessageDto } from "../../../services/dto/message";

type Props = {

    chat: ChatDto | null;

    messages: MessageDto[];

    loading: boolean;

    text: string;

    error: string | null;

    onTextChange(value:string):void;

    onSend():void;

    onDelete(id:number):void;

    onBack():void;

    onEdit():void;
};


export default function ChatContent(props:Props) {


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

        messages={props.messages}

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