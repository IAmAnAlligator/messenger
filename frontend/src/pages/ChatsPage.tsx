import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api/client";
import { useAuth } from "../auth/AuthContext";

import {
    connectSocket,
    subscribe,
    unsubscribe
} from "../websocket/chatSocket";

type UserDto = {
    id: number;
    username: string;
    role: string;
};

type ChatMemberDto = {
    user: UserDto;
    chatRole: string;
    joinedAt: string;
};

type ChatDto = {
    id: number;
    name: string;
    type: "PRIVATE" | "GROUP";
    members: ChatMemberDto[];
};

export default function ChatsPage() {

    const [chats, setChats] =
        useState<ChatDto[]>([]);

    const [loading, setLoading] =
        useState(true);

    const {
        user,
        logout
    } = useAuth();

    const navigate =
        useNavigate();

    useEffect(() => {
        loadChats();
    }, []);

    useEffect(() => {

        const token =
            localStorage.getItem("accessToken");

        if (!token) {
            return;
        }

        connectSocket(token);

        subscribe("/topic/chat.created", () => {
            // Загружаем список заново, чтобы получить полные данные чата
            loadChats();
        });

        return () => {
            unsubscribe("/topic/chat.created");
        };

    }, []);

    useEffect(() => {

        const token =
            localStorage.getItem("accessToken");

        if (!token) {
            return;
        }

        connectSocket(token);

        subscribe("/topic/chat.deleted", message => {

            const event = JSON.parse(message.body);

            setChats(prev =>
                prev.filter(chat =>
                    chat.id !== event.chatId
                )
            );

        });

        return () => {
            unsubscribe("/topic/chat.deleted");
        };

    }, []);

    async function loadChats() {

        try {

            setLoading(true);

            const res =
                await api.get("/chats");

            setChats(res.data);

        } catch (e) {

            console.error(e);

        } finally {

            setLoading(false);
        }
    }

    async function handleLogout() {

        await logout();

        navigate("/", {
            replace: true
        });
    }

    function getChatName(chat: ChatDto): string {

        if (chat.type === "GROUP") {
            return chat.name;
        }

        if (!user) {
            return chat.name;
        }

        const otherMember = chat.members.find(
            member => member.user.id !== user.id
        );

        return otherMember?.user.username ?? chat.name;
    }

    return (

        <div style={{ padding: 20 }}>

            <div
                style={{
                    display: "flex",
                    justifyContent: "space-between",
                    alignItems: "center"
                }}
            >
                <h2>Chats</h2>

                <button onClick={handleLogout}>
                    Logout
                </button>

            </div>

            <button
                onClick={() => navigate("/chats/create")}
                style={{
                    marginBottom: 20
                }}
            >
                Create chat
            </button>

            <hr />

            {loading && <p>Loading...</p>}

            {!loading && chats.length === 0 && (
                <p>No chats</p>
            )}

            {chats.map(chat => (

                <div
                    key={chat.id}
                    onClick={() =>
                        navigate(`/chats/${chat.id}`)
                    }
                    style={{
                        padding: 12,
                        marginBottom: 10,
                        border: "1px solid #ddd",
                        cursor: "pointer"
                    }}
                >
                    {getChatName(chat)}
                </div>

            ))}

        </div>

    );
}