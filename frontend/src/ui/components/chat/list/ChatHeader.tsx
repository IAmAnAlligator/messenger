import type { ChatDto } from "../../../../types/chat";


interface Props {
    chat: ChatDto | null;
    currentUserId: string;
    isOnline: boolean;
    onBack: () => void;
    onEdit: () => void;
}


export default function ChatHeader({
    chat,
    currentUserId,
    isOnline,
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
            chat.members.find(
                member => member.user.id !== currentUserId
            )?.user.username
            ?? "Private chat"
        );
    }

    const showOnline =
        chat?.type === "PRIVATE" && isOnline;

    return (
        <div className="chat-header">

            <button onClick={onBack}>
                ← Back
            </button>

            <div className="chat-header-info">
                <h2>
                    {getTitle()}
                </h2>

                {showOnline && (
                    <span className="online-status">
                        <span className="online-status-dot" />
                        Online
                    </span>
                )}
            </div>

            <button onClick={onEdit}>
                ⚙ Edit
            </button>

        </div>
    );
}