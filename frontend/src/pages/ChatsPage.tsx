import { useEffect, useState } from "react";
import { api } from "../api/client";

type ChatDto = {
    id: number;
    name: string;
};

export default function ChatsPage() {

    const [chats, setChats] = useState<ChatDto[]>([]);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        loadChats();
    }, []);

    async function loadChats() {
        try {
            setLoading(true);

            const response = await api.get("/chats");

            setChats(response.data);

        } catch (e) {
            console.error("Failed to load chats", e);
        } finally {
            setLoading(false);
        }
    }

    return (
        <div style={{ padding: 20 }}>

            <h2>Your chats</h2>

            {loading && <p>Loading...</p>}

            {!loading && chats.length === 0 && (
                <p>No chats yet</p>
            )}

            <div>
                {chats.map((chat) => (
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