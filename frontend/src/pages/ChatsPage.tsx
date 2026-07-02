import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api/client";
import { useAuth } from "../auth/AuthContext";

import {
    connectSocket,
    disconnectSocket,
    subscribe,
    unsubscribe
} from "../websocket/chatSocket";

type ChatDto = {
    id: number;
    name: string;
};

type ChatType = "PRIVATE" | "GROUP";

type UserDto = {
    id: number;
    username: string;
    role: string;
};

export default function ChatsPage() {

    const [chats, setChats] =
        useState<ChatDto[]>([]);

    const [loading, setLoading] =
        useState(true);

    const { logout } =
        useAuth();

    const navigate =
        useNavigate();

    const [type, setType] =
        useState<ChatType>("GROUP");

    const [name, setName] =
        useState("");

    const [search, setSearch] =
        useState("");

    const [users, setUsers] =
        useState<UserDto[]>([]);

    const [selectedUsers, setSelectedUsers] =
        useState<UserDto[]>([]);

    const [searchLoading, setSearchLoading] =
        useState(false);

    useEffect(() => {
        loadChats();
    }, []);

    useEffect(() => {

    const token = localStorage.getItem("accessToken");
    if (!token) return;

    connectSocket(token);

    subscribe("/topic/chat.created", message => {

        const event = JSON.parse(message.body);

        setChats(prev => {

            if (prev.some(c => c.id === event.chatId)) {
                return prev;
            }

            return [
                {
                    id: event.chatId,
                    name: event.name
                },
                ...prev
            ];
        });

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

        // безопасная подписка (без дублей и ручного socket access)
        subscribe("/topic/chat.deleted", message => {

            const event = JSON.parse(message.body);

            setChats(prev =>
                prev.filter(
                    chat => chat.id !== event.chatId
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

            const res = await api.get("/chats");

            setChats(res.data);

        } catch (e) {
            console.error(e);
        } finally {
            setLoading(false);
        }
    }

    useEffect(() => {

        const timer = setTimeout(() => {

            const q = search.trim();

            if (q) {
                searchUsers(q);
            } else {
                setUsers([]);
            }

        }, 300);

        return () => clearTimeout(timer);

    }, [search]);

    async function searchUsers(query: string) {

        try {
            setSearchLoading(true);

            const res = await api.get(
                "/users/search",
                { params: { query } }
            );

            setUsers(
                Array.isArray(res.data)
                    ? res.data
                    : []
            );

        } finally {
            setSearchLoading(false);
        }
    }

    function addUser(user: UserDto) {

        setSelectedUsers(prev => {

            if (type === "PRIVATE") {
                return [user];
            }

            if (prev.find(u => u.id === user.id)) {
                return prev;
            }

            return [...prev, user];
        });

        setSearch("");
        setUsers([]);
    }

    function removeUser(id: number) {
        setSelectedUsers(prev =>
            prev.filter(u => u.id !== id)
        );
    }

    async function createChat() {

        try {

            await api.post("/chats", {
                type,
                name: type === "GROUP" ? name : null,
                memberIds: selectedUsers.map(u => u.id)
            });

            setSelectedUsers([]);
            setUsers([]);
            setName("");

            await loadChats();

        } catch (e) {
            console.error(e);
        }
    }

    async function handleLogout() {

        await logout();

        navigate("/", { replace: true });

    }

    return (
        <div style={{ padding: 20 }}>

            <div style={{
                display: "flex",
                justifyContent: "space-between"
            }}>
                <h2>Chats</h2>

                <button onClick={handleLogout}>
                    Logout
                </button>
            </div>

            <h3>Create chat</h3>

            <select
                value={type}
                onChange={e =>
                    setType(e.target.value as ChatType)
                }
            >
                <option value="GROUP">GROUP</option>
                <option value="PRIVATE">PRIVATE</option>
            </select>

            {type === "GROUP" && (
                <input
                    value={name}
                    placeholder="name"
                    onChange={e => setName(e.target.value)}
                />
            )}

            <div>

                <input
                    value={search}
                    placeholder="Search users"
                    onChange={e => setSearch(e.target.value)}
                />

                {searchLoading && <p>Searching...</p>}

                {users.map(user => (
                    <div
                        key={user.id}
                        onClick={() => addUser(user)}
                    >
                        {user.username}
                    </div>
                ))}

            </div>

            <div>

                {selectedUsers.map(u => (
                    <div key={u.id}>
                        {u.username}
                        <button onClick={() => removeUser(u.id)}>
                            x
                        </button>
                    </div>
                ))}

            </div>

            <button onClick={createChat}>
                Create
            </button>

            <hr />

            {loading && <p>Loading...</p>}

            {chats.map(chat => (
                <div
                    key={chat.id}
                    onClick={() => navigate(`/chats/${chat.id}`)}
                    style={{
                        padding: 12,
                        marginBottom: 10,
                        border: "1px solid #ddd",
                        cursor: "pointer"
                    }}
                >
                    {chat.name}
                </div>
            ))}

        </div>
    );
}