import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { api } from "../api/client";

type ChatType = "PRIVATE" | "GROUP";

type UserDto = {
    id: number;
    username: string;
    role: string;
};

export default function ChatCreatePage() {

    const navigate = useNavigate();

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
                {
                    params: {
                        query
                    }
                }
            );

            setUsers(
                Array.isArray(res.data)
                    ? res.data
                    : []
            );

        } catch (e) {

            console.error(e);

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
                name: type === "GROUP"
                    ? name
                    : null,
                memberIds:
                    selectedUsers.map(u => u.id)
            });

            navigate("/chats");

        } catch (e) {

            console.error(e);
        }
    }

    return (

        <div style={{ padding: 20 }}>

            <h2>Create chat</h2>

            <button
                onClick={() => navigate("/chats")}
                style={{ marginBottom: 20 }}
            >
                Back
            </button>

            <div style={{ marginBottom: 15 }}>

                <select
                    value={type}
                    onChange={e =>
                        setType(e.target.value as ChatType)
                    }
                >
                    <option value="GROUP">
                        GROUP
                    </option>

                    <option value="PRIVATE">
                        PRIVATE
                    </option>

                </select>

            </div>

            {type === "GROUP" && (

                <div style={{ marginBottom: 15 }}>

                    <input
                        value={name}
                        placeholder="Chat name"
                        onChange={e =>
                            setName(e.target.value)
                        }
                    />

                </div>

            )}

            <div style={{ marginBottom: 15 }}>

                <input
                    value={search}
                    placeholder="Search users"
                    onChange={e =>
                        setSearch(e.target.value)
                    }
                />

                {searchLoading && (
                    <p>Searching...</p>
                )}

                {users.map(user => (

                    <div
                        key={user.id}
                        style={{
                            cursor: "pointer",
                            padding: 6,
                            borderBottom: "1px solid #ddd"
                        }}
                        onClick={() => addUser(user)}
                    >
                        {user.username}
                    </div>

                ))}

            </div>

            <div style={{ marginBottom: 20 }}>

                {selectedUsers.map(user => (

                    <div
                        key={user.id}
                        style={{
                            display: "flex",
                            alignItems: "center",
                            gap: 10,
                            marginBottom: 5
                        }}
                    >
                        {user.username}

                        <button
                            onClick={() => removeUser(user.id)}
                        >
                            ×
                        </button>

                    </div>

                ))}

            </div>

            <button
                onClick={createChat}
                disabled={
                    selectedUsers.length === 0 ||
                    (type === "GROUP" && !name.trim())
                }
            >
                Create
            </button>

        </div>

    );
}