import ChatHeader from "../list/ChatHeader";
import MessageList from "./MessageList";
import MessageInput from "./MessageInput";
import WsError from "../../common/WsError";

import type { ChatDto } from "../../../../types/chat";
import type { MessageDto } from "../../../../types/message";

type Props = {
    chat: ChatDto | null;
    messages: MessageDto[];

    loading: boolean;
    loadingMore: boolean;
    hasMore: boolean;

    currentUserId: string;
    isOnline: boolean;

    isMessageReadByOtherUser(messageId: string): boolean;

    text: string;
    error: string | null;

    onSendFile(file: File): Promise<void>;
    onLoadMore(): void | Promise<void>;
    onReadUpTo(messageId: string): void;
    onTextChange(value: string): void;
    onSend(): void;
    onDelete(id: string): void;
    onUserProfile(userId: string): void;
    onBack(): void;
    onEdit(): void;
};

export default function ChatContent({
    chat,
    messages,
    loading,
    loadingMore,
    hasMore,
    currentUserId,
    isOnline,
    isMessageReadByOtherUser,
    text,
    error,
    onSendFile,
    onLoadMore,
    onReadUpTo,
    onTextChange,
    onSend,
    onDelete,
    onUserProfile,
    onBack,
    onEdit,
}: Props) {

    const lastReadMessageId =
        chat?.members.find(
            member => member.user.id === currentUserId
        )?.lastReadMessageId ?? null;

    return (
        <div className="chat-page">

            <ChatHeader
                chat={chat}
                currentUserId={currentUserId}
                isOnline={isOnline}
                onBack={onBack}
                onEdit={onEdit}
            />

            <WsError message={error} />

            {chat && (
                <MessageList
                    key={chat.id}
                    loading={loading}
                    loadingMore={loadingMore}
                    hasMore={hasMore}
                    messages={messages}
                    currentUserId={currentUserId}
                    currentUserLastReadMessageId={lastReadMessageId}
                    isMessageReadByOtherUser={
                        isMessageReadByOtherUser
                    }
                    onLoadMore={onLoadMore}
                    onReadUpTo={onReadUpTo}
                    onDelete={onDelete}
                    onUserProfile={onUserProfile}
                />
            )}

            <MessageInput
                value={text}
                onChange={onTextChange}
                onSend={onSend}
                onSendFile={onSendFile}
            />

        </div>
    );
}