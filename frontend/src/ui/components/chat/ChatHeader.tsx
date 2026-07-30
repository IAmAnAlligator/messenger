import type { ChatDto } from "../../../services/dto/chat";


interface Props {

    chat: ChatDto | null;

    onBack: () => void;

    onEdit: () => void;
}


export default function ChatHeader({
    chat,
    onBack,
    onEdit

}: Props) {


    function getTitle() {

        if (!chat) {
            return "Chat";
        }


        if (chat.type === "GROUP") {
            return chat.name ?? "Group chat";
        }


        return (
            chat.members[0]
                ?.user
                .username
            ??
            "Private chat"
        );
    }


    return (

        <div className="chat-header">


            <button onClick={onBack}>
                ← Back
            </button>



            <h2>
                {getTitle()}
            </h2>



            <button onClick={onEdit}>
                ⚙ Edit
            </button>


        </div>

    );
}