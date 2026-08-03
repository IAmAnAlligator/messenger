import ChatItem from "./ChatItem";

import type {
    ChatDto
} from "../../../../hooks/useChats";


type Props = {

    chats:ChatDto[];

    getName(chat:ChatDto):string;

    onOpen(id:number):void;

};



export default function ChatList({

    chats,

    getName,

    onOpen

}:Props){



return (

<div className="chat-list">


{
    chats.map(chat => (

        <ChatItem

            key={chat.id}

            chat={chat}

            name={
                getName(chat)
            }

            onClick={() =>
                onOpen(chat.id)
            }

        />

    ))
}


</div>

);


}