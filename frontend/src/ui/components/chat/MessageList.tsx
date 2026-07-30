import {
    useEffect,
    useRef
} from "react";

import MessageItem
    from "./MessageItem";

import type {
    MessageDto
} from "../../../services/dto/message";

import "../../styles/chatPage.css";


type Props = {

    loading:boolean;

    messages:MessageDto[];

    onDelete(id:number):void;

};


export default function MessageList({

    loading,

    messages,

    onDelete

}:Props){


const bottomRef =
    useRef<HTMLDivElement>(null);


useEffect(()=>{

    bottomRef.current?.scrollIntoView({
        behavior:"smooth"
    });

},[messages]);



const sortedMessages =
    [...messages].sort(
        (a, b) =>
            new Date(a.createdAt).getTime()
            -
            new Date(b.createdAt).getTime()
    );



if(loading){

    return (
        <div className="messages">
            Loading...
        </div>
    );

}


return (

<div className="messages">


    {
    
    sortedMessages.map(message=>(

        <MessageItem

            key={message.id}

            message={message}

            onDelete={onDelete}

        />

    ))}


    <div ref={bottomRef}/>


</div>

);

}