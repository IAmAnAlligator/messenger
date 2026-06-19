import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api/client";
import { useAuth } from "../auth/AuthContext";

/**
 * 💬 Chat DTO from backend
 */
type ChatDto = {
    id: number;
    name: string;
};

type ChatType = "PRIVATE" | "GROUP";

/**
 * 👤 User DTO for search
 */
type UserDto = {
    id: number;
    username: string;
    role: string;
};

export default function ChatsPage() {

    // =========================
    // 💬 STATE
    // =========================
    const [chats, setChats] = useState<ChatDto[]>([]);
    const [loading, setLoading] = useState(true);

    const { logout } = useAuth();
    const navigate = useNavigate();

    // =========================
    // 🧠 CREATE CHAT STATE
    // =========================
    const [type, setType] = useState<ChatType>("GROUP");
    const [name, setName] = useState("");

    // =========================
    // 🔎 USER SEARCH STATE
    // =========================
    const [search, setSearch] = useState("");
    const [users, setUsers] = useState<UserDto[]>([]);
    const [selectedUsers, setSelectedUsers] = useState<UserDto[]>([]);
    const [searchLoading, setSearchLoading] = useState(false);

    // =========================
    // 🚀 LOAD CHATS
    // =========================
    useEffect(() => {
        loadChats();
    }, []);

    async function loadChats() {
        try {
            setLoading(true);
            const res = await api.get("/chats");
            setChats(res.data);
        } catch (e) {
            console.error("Failed to load chats", e);
        } finally {
            setLoading(false);
        }
    }

    // =========================
    // 🔎 DEBOUNCED SEARCH
    // =========================
    useEffect(() => {
        const timeout = setTimeout(() => {
            const q = search.trim();

            if (q.length > 0) {
                searchUsers(q);
            } else {
                setUsers([]);
            }
        }, 300);

        return () => clearTimeout(timeout);
    }, [search]);

    async function searchUsers(query: string) {
        try {
            setSearchLoading(true);

            const res = await api.get("/users/search", {
                params: { query }
            });

            setUsers(Array.isArray(res.data) ? res.data : []);
        } catch (e) {
            console.error("User search failed", e);
        } finally {
            setSearchLoading(false);
        }
    }

    // =========================
    // ➕ ADD USER
    // =========================
    function addUser(user: UserDto) {
        setSelectedUsers(prev => {

            // 🔒 PRIVATE restriction
            if (type === "PRIVATE") {
                return [user];
            }

            if (prev.find(u => u.id === user.id)) return prev;

            return [...prev, user];
        });

        setSearch("");
        setUsers([]);
    }

    // =========================
    // ❌ REMOVE USER
    // =========================
    function removeUser(id: number) {
        setSelectedUsers(prev =>
            prev.filter(u => u.id !== id)
        );
    }

    // =========================
    // 🆕 CREATE CHAT
    // =========================
    async function createChat() {
        try {
            const memberIds = selectedUsers.map(u => u.id);

            await api.post("/chats", {
                type,
                name: type === "GROUP" ? name : null,
                memberIds
            });

            setSelectedUsers([]);
            setSearch("");
            setUsers([]);
            setName("");

            await loadChats();

        } catch (e) {
            console.error("Failed to create chat", e);
        }
    }

    // =========================
    // 🚪 LOGOUT
    // =========================
    async function handleLogout() {
        await logout();
        navigate("/", { replace: true });
    }

    // =========================
    // 🎨 UI
    // =========================
    return (
        <div style={{ padding: 20 }}>

            {/* HEADER */}
            <div style={{
                display: "flex",
                justifyContent: "space-between",
                marginBottom: 20
            }}>
                <h2>Your chats</h2>

                <button onClick={handleLogout}>
                    Logout
                </button>
            </div>

            {/* =========================
                CREATE CHAT
            ========================= */}
            <div style={{
                padding: 10,
                border: "1px solid #ddd",
                borderRadius: 8,
                marginBottom: 20
            }}>
                <h3>Create chat</h3>

                {/* TYPE */}
                <select
                    value={type}
                    onChange={(e) => {
                        const newType = e.target.value as ChatType;
                        setType(newType);

                        // 🔒 keep only 1 user for PRIVATE
                        setSelectedUsers(prev =>
                            newType === "PRIVATE" ? prev.slice(0, 1) : prev
                        );
                    }}
                >
                    <option value="GROUP">GROUP</option>
                    <option value="PRIVATE">PRIVATE</option>
                </select>

                {/* GROUP NAME */}
                {type === "GROUP" && (
                    <input
                        placeholder="Chat name"
                        value={name}
                        onChange={(e) => setName(e.target.value)}
                        style={{ marginLeft: 10 }}
                    />
                )}

                {/* =========================
                    USER SEARCH
                ========================= */}
                <div style={{ marginTop: 10 }}>
                    <input
                        placeholder="Search users..."
                        value={search}
                        onChange={(e) => setSearch(e.target.value)}
                    />

                    {searchLoading && <p>Searching...</p>}

                    {/* PRIVATE warning */}
                    {type === "PRIVATE" && selectedUsers.length >= 1 && (
                        <p style={{ color: "orange" }}>
                            PRIVATE chat allows only 1 user
                        </p>
                    )}

                    {/* SEARCH RESULTS */}
                    {users.length > 0 && (
                        <div style={{
                            border: "1px solid #ddd",
                            marginTop: 5
                        }}>
                            {users.map(user => (
                                <div
                                    key={user.id}
                                    style={{
                                        padding: 6,
                                        cursor: "pointer",
                                        opacity:
                                            type === "PRIVATE" &&
                                            selectedUsers.length >= 1
                                                ? 0.5
                                                : 1
                                    }}
                                    onClick={() => {
                                        if (
                                            type === "PRIVATE" &&
                                            selectedUsers.length >= 1
                                        ) return;

                                        addUser(user);
                                    }}
                                >
                                    {user.username} (id: {user.id})
                                </div>
                            ))}
                        </div>
                    )}
                </div>

                {/* =========================
                    SELECTED USERS
                ========================= */}
                <div style={{ marginTop: 10 }}>
                    <b>Selected users:</b>

                    {selectedUsers.length === 0 && (
                        <p>none</p>
                    )}

                    {selectedUsers.map(user => (
                        <div
                            key={user.id}
                            style={{ display: "flex", gap: 10 }}
                        >
                            <span>{user.username}</span>

                            <button onClick={() => removeUser(user.id)}>
                                remove
                            </button>
                        </div>
                    ))}
                </div>

                {/* CREATE BUTTON */}
                <button
                    onClick={createChat}
                    disabled={
                        type === "PRIVATE" &&
                        selectedUsers.length !== 1
                    }
                    style={{
                        marginTop: 10,
                        opacity:
                            type === "PRIVATE" &&
                            selectedUsers.length !== 1
                                ? 0.5
                                : 1,
                        cursor:
                            type === "PRIVATE" &&
                            selectedUsers.length !== 1
                                ? "not-allowed"
                                : "pointer"
                    }}
                >
                    Create
                </button>
            </div>

            {/* =========================
                CHAT LIST
            ========================= */}
            {loading && <p>Loading...</p>}

            {!loading && chats.length === 0 && (
                <p>No chats yet</p>
            )}

            <div>
                {chats.map(chat => (
                    <div
                        key={chat.id}
                        style={{
                            padding: 10,
                            margin: 5,
                            border: "1px solid #ccc",
                            borderRadius: 6
                        }}
                    >
                        {chat.name}
                    </div>
                ))}
            </div>
        </div>
    );
}