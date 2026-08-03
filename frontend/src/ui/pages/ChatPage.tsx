import { useParams } from "react-router-dom";

import ChatContainer from "../components/chat/view/ChatContainer";

export default function ChatPage() {

    const { chatId } = useParams();

    if (!chatId) {
        return <div>Chat not found</div>;
    }

    return (
        <ChatContainer
            chatId={Number(chatId)}
        />
    );
}