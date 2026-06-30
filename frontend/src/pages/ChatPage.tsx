import {
    useEffect,
    useState
} from "react";

import {
    useNavigate,
    useParams
} from "react-router-dom";

import { api } from "../api/client";

import {
    connectSocket,
    disconnectSocket,
    getSocket
} from "../websocket/chatSocket";

import {
    useAuth
} from "../auth/AuthContext";

type UserDto = {
    id: number;
    username: string;
    role: string;
};

type MessageDto = {
    id: number;
    chatId: number;
    sender: UserDto;
    content: string;
    createdAt: string;
    status: "SENT" | "READ";
};

type DeleteMessageEvent = {
    type: "MESSAGE_DELETED";
    messageId: number;
};

type ChatDeletedEvent = {
    type: "CHAT_DELETED";
    chatId: number;
};

export default function ChatPage() {

    const { chatId } = useParams();
    const navigate = useNavigate();
    const { user } = useAuth();

    const [messages, setMessages] = useState<MessageDto[]>([]);
    const [text, setText] = useState("");
    const [loading, setLoading] = useState(true);

    function sortMessages(items: MessageDto[]) {
        return [...items].sort(
            (a, b) =>
                new Date(a.createdAt).getTime() -
                new Date(b.createdAt).getTime()
        );
    }

    useEffect(() => {

        const token = localStorage.getItem("accessToken");

        if (token) {
            connectSocket(token, async () => {
                subscribeMessages();
                await loadMessages();
            });
        }

        return () => {
            disconnectSocket();
        };

    }, [chatId]);

    async function loadMessages() {

        try {

            setLoading(true);

            const res = await api.get(
                `/chats/${chatId}/messages`
            );

            const loaded = Array.isArray(res.data)
                ? res.data
                : [];

            setMessages(sortMessages(loaded));
            sendReadEvents(loaded);

        } catch (e) {
            console.error(e);
        } finally {
            setLoading(false);
        }
    }

    function deleteMessage(messageId: number) {

        const socket = getSocket();

        if (!socket?.connected) return;

        socket.publish({
            destination: "/app/chat.delete",
            body: JSON.stringify({
                id: messageId,
                chatId: Number(chatId)
            })
        });

    }

    function sendReadEvents(loaded: MessageDto[]) {

        if (!user) return;

        const socket = getSocket();

        if (!socket?.connected) return;

        loaded
            .filter(m =>
                m.sender.id !== user.id &&
                m.status === "SENT"
            )
            .forEach(m => {

                socket.publish({
                    destination: "/app/chat.read",
                    body: JSON.stringify({
                        id: m.id,
                        chatId: Number(chatId)
                    })
                });

            });

    }

    function subscribeMessages() {

        const socket = getSocket();

        if (!socket) return;

        socket.subscribe(
            `/topic/chat/${chatId}`,
            frame => {

                const incoming: any = JSON.parse(frame.body);

                // CHAT DELETED
                if (
                    typeof incoming === "object" &&
                    "type" in incoming &&
                    incoming.type === "CHAT_DELETED"
                ) {
                    navigate("/chats", { replace: true });
                    return;
                }

                // MESSAGE DELETED
                if (
                    typeof incoming === "object" &&
                    "type" in incoming &&
                    incoming.type === "MESSAGE_DELETED"
                ) {
                    const event = incoming as DeleteMessageEvent;

                    setMessages(prev =>
                        prev.filter(m => m.id !== event.messageId)
                    );

                    return;
                }

                // NORMAL MESSAGE
                const message = incoming as MessageDto;

                setMessages(prev => {

                    const index = prev.findIndex(x => x.id === message.id);

                    if (index !== -1) {

                        const updated = [...prev];

                        updated[index] = {
                            ...updated[index],
                            ...message
                        };

                        return sortMessages(updated);
                    }

                    const next = sortMessages([...prev, message]);

                    if (
                        user &&
                        message.sender.id !== user.id &&
                        message.status === "SENT"
                    ) {
                        setTimeout(() => {
                            socket.publish({
                                destination: "/app/chat.read",
                                body: JSON.stringify({
                                    id: message.id,
                                    chatId: Number(chatId)
                                })
                            });
                        }, 0);
                    }

                    return next;
                });

            },
            {
                id: `chat-${chatId}`
            }
        );

    }

    function sendMessage() {

        const content = text.trim();
        if (!content) return;

        const socket = getSocket();
        if (!socket?.connected) return;

        socket.publish({
            destination: "/app/chat.send",
            body: JSON.stringify({
                chatId: Number(chatId),
                content
            })
        });

        setText("");
    }

    return (

        <div style={{ padding: 20 }}>

            <div style={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                marginBottom: 20
            }}>

                <button onClick={() => navigate("/chats")}>
                    ← Back
                </button>

                <h2>Chat {chatId}</h2>

                <button onClick={() =>
                    navigate(`/chats/${chatId}/edit`)
                }>
                    ⚙ Edit chat
                </button>

            </div>

            <div style={{
                border: "1px solid #ddd",
                minHeight: 500,
                padding: 16,
                marginBottom: 20
            }}>

                {loading && <p>Loading...</p>}

                {messages.map(m => (
                    <div key={m.id} style={{
                        marginBottom: 12,
                        borderBottom: "1px solid #eee",
                        paddingBottom: 8
                    }}>

                        <div style={{
                            display: "flex",
                            justifyContent: "space-between",
                            alignItems: "center"
                        }}>

                            <div>
                                <b>{m.sender.username}</b>{" · "}{m.status}
                            </div>

                            {user?.id === m.sender.id && (
                                <button
                                    onClick={() => deleteMessage(m.id)}
                                >
                                    🗑 Delete
                                </button>
                            )}

                        </div>

                        <div>{m.content}</div>

                        <small>
                            {new Date(m.createdAt).toLocaleString()}
                        </small>

                    </div>
                ))}

            </div>

            <div style={{ display: "flex", gap: 10 }}>

                <input
                    value={text}
                    onChange={e => setText(e.target.value)}
                    placeholder="Type message..."
                    style={{ flex: 1 }}
                />

                <button onClick={sendMessage}>
                    Send
                </button>

            </div>

        </div>
    );
}