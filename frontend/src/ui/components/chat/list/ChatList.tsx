
import type {
    ChatDto
} from "../../../../hooks/useChats";

import {
    useAuth
} from "../../../../contexts/AuthContext";


type Props = {
    chats: ChatDto[];

    getName(
        chat: ChatDto
    ): string;

    onOpen(
        chatId: string
    ): void;

    hasNext: boolean;

    loadingMore: boolean;

    loadMore(): void;
};


export default function ChatList({
    chats,
    getName,
    onOpen,
    hasNext,
    loadingMore,
    loadMore
}: Props) {

    const { user } = useAuth();


    function formatLastMessage(
        chat: ChatDto
    ): string {

        if (!chat.lastMessage) {
            return "";
        }

        const prefix =
            chat.lastMessage.sender.id === user?.id
                ? "you: "
                : `${chat.lastMessage.sender.username}: `;

        if (
            chat.lastMessage.content === null
        ) {
            return `${prefix}📎`;
        }

        const preview =
            chat.lastMessage.content.length > 15
                ? `${chat.lastMessage.content.slice(0, 15)}...`
                : chat.lastMessage.content;

        return `${prefix}${preview}`;
    }


    function formatLastMessageDate(
        chat: ChatDto
    ): string {

        if (!chat.lastMessage) {
            return "";
        }

        return new Date(
            chat.lastMessage.createdAt
        ).toLocaleString(
            "en-US",
            {
                weekday: "short",
                month: "short",
                day: "numeric",
                hour: "2-digit",
                minute: "2-digit",
                hour12: false
            }
        );
    }


    function getLastMessageStatus(
        chat: ChatDto
    ): string | null {

        if (!chat.lastMessage) {
            return null;
        }

        if (
            chat.lastMessage.sender.id !== user?.id
        ) {
            return null;
        }

        if (
            chat.lastMessageStatus === "READ"
        ) {
            return "✓✓";
        }

        if (
            chat.lastMessageStatus === "SENT"
        ) {
            return "✓";
        }

        return null;
    }


    return (
        <div className="chat-list">

            {chats.map(
                chat => {

                    const status =
                        getLastMessageStatus(chat);

                    return (
                        <div
                            key={chat.id}
                            className="chat-item"
                            onClick={() => onOpen(chat.id)}
                        >

                            <div className="chat-info">

                                <div className="chat-name-container">

                                    <span className="chat-name">
                                        {getName(chat)}
                                    </span>

                                    {chat.unreadCount > 0 && (
                                        <span className="chat-unread-badge">
                                            {chat.unreadCount}
                                        </span>
                                    )}

                                </div>




                                {chat.lastMessage && (
                                    <>
                                        <div className="chat-last-message">
                                            {formatLastMessage(chat)}
                                        </div>

                                        <div className="chat-message-date">
                                            {formatLastMessageDate(chat)}
                                        </div>

                                    </>
                                )}

                                {status && (
    <div
        className={
            chat.lastMessageStatus === "READ"
                ? "chat-message-status chat-message-status-read"
                : "chat-message-status"
        }
    >
        {status}
    </div>
)}

                            </div>

                        </div>
                    );
                }
            )}


            {hasNext && (
                <button
                    onClick={loadMore}
                    disabled={loadingMore}
                >
                    {loadingMore
                        ? "Loading..."
                        : "Load more"}
                </button>
            )}

        </div>
    );
}
