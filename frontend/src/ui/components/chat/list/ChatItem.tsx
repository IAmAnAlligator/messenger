import type {
    ChatDto
} from "../../../../hooks/useChats";


type Props = {

    chat: ChatDto;

    name: string;

    unread: boolean;

    onClick(): void;

};


export default function ChatItem({

    name,

    unread,

    onClick

}: Props) {


    return (

        <div
            className="chat-item"
            onClick={onClick}
        >

            <div className="chat-name-container">

                <span className="chat-name">
                    {name}
                </span>

                {unread && (
                    <span className="chat-unread-indicator" />
                )}

            </div>

        </div>

    );

}