import { useParams } from "react-router-dom";

import ChatContainer from "../containers/ChatContainer";

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