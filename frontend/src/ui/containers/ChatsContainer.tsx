import {
    useEffect,
    useMemo,
    useState
} from "react";

import { useNavigate } from "react-router-dom";

import { useAuth } from "../../contexts/AuthContext";
import { useChats } from "../../hooks/useChats";

import type { ChatDto } from "../../hooks/useChats";

import ChatsHeader from "../components/chat/view/ChatsHeader";
import ChatList from "../components/chat/list/ChatList";

import {
    subscribe,
    unsubscribe
} from "../../services/chatSocket";

import "../styles/chatsPage.css";

type PresenceStatus = {
    userId: string;
    online: boolean;
};

export default function ChatsContainer() {
    const navigate = useNavigate();

    const { user, logout } = useAuth();

    const {
        chats,
        loading,
        loadingMore,
        hasNext,
        loadMore
    } = useChats();

    const [onlineUsers, setOnlineUsers] = useState<
        Record<string, boolean>
    >({});

    const otherUserIds = useMemo(() => {
        if (!user) {
            return [];
        }

        return [
            ...new Set(
                chats
                    .filter(chat => chat.type === "PRIVATE")
                    .map(chat =>
                        chat.members.find(
                            member => member.user.id !== user.id
                        )?.user.id
                    )
                    .filter((id): id is string => Boolean(id))
            )
        ];
    }, [chats, user]);

    useEffect(() => {
        setOnlineUsers({});

        if (otherUserIds.length === 0) {
            return;
        }

        const subscriptions: {
            destination: string;
            handler: (message: { body: string }) => void;
        }[] = [];

        function handlePresence(message: { body: string }) {
            try {
                const presence = JSON.parse(
                    message.body
                ) as PresenceStatus;

                if (!otherUserIds.includes(presence.userId)) {
                    return;
                }

                setOnlineUsers(previous => ({
                    ...previous,
                    [presence.userId]: presence.online
                }));
            } catch (error) {
                console.error(
                    "Failed to parse presence event",
                    error
                );
            }
        }

        for (const userId of otherUserIds) {
            const destinations = [
                `/topic/presence/user/${userId}`,
                `/app/presence/user/${userId}`
            ];

            for (const destination of destinations) {
                subscribe(destination, handlePresence);

                subscriptions.push({
                    destination,
                    handler: handlePresence
                });
            }
        }

        return () => {
            for (const subscription of subscriptions) {
                unsubscribe(
                    subscription.destination,
                    subscription.handler
                );
            }
        };
    }, [otherUserIds]);

    function handleLogout() {
        logout();
        navigate("/");
    }

    function handleProfile() {
        navigate("/profile");
    }

    function handleSettings() {
        navigate("/settings");
    }

    function getChatName(chat: ChatDto): string {
        if (chat.type === "GROUP") {
            return chat.name;
        }

        const otherMember = chat.members.find(
            member => member.user.id !== user?.id
        );

        return otherMember?.user.username ?? chat.name;
    }

    function isUserOnline(chat: ChatDto): boolean {
        if (chat.type !== "PRIVATE" || !user) {
            return false;
        }

        const otherMember = chat.members.find(
            member => member.user.id !== user.id
        );

        if (!otherMember) {
            return false;
        }

        return onlineUsers[otherMember.user.id] ?? false;
    }

    function handleOpenChat(chatId: string) {
        navigate(`/chats/${chatId}`);
    }

    function handleNewGroup() {
        navigate("/chats/create/group");
    }

    function handleNewPrivate() {
        navigate("/chats/create/private");
    }

    return (
        <div className="chats-container">
            <ChatsHeader
                onLogout={handleLogout}
                onProfile={handleProfile}
                onNotificationSettings={handleSettings}
            />

            <div className="chats-actions">
                <button onClick={handleNewGroup}>
                    New group
                </button>

                <button onClick={handleNewPrivate}>
                    New chat
                </button>
            </div>

            {loading && (
                <div className="chat-loader">
                    Loading...
                </div>
            )}

            {!loading && chats.length === 0 && (
                <div className="chat-empty">
                    No chats
                </div>
            )}

            {!loading && chats.length > 0 && (
                <ChatList
                    chats={chats}
                    getName={getChatName}
                    isUserOnline={isUserOnline}
                    onOpen={handleOpenChat}
                    hasNext={hasNext}
                    loadingMore={loadingMore}
                    loadMore={loadMore}
                />
            )}
        </div>
    );
}