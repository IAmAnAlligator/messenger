import type {
    ChatDto
} from "../../../hooks/useChats";


type Props = {

    chat:ChatDto;

    name:string;

    onClick():void;

};


export default function ChatItem({

    chat,

    name,

    onClick

}:Props){


return (

<div

    className="chat-item"

    onClick={onClick}

>

    {name}

</div>


);


}