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

    status:
        | "SENT"
        | "READ";
};

export default function ChatPage() {

    const { chatId } =
        useParams();

    const navigate =
        useNavigate();

    const {
        user
    } =
        useAuth();

    const [messages,
        setMessages] =
        useState<MessageDto[]>([]);

    const [text,
        setText] =
        useState("");

    const [loading,
        setLoading] =
        useState(true);

    function sortMessages(
        items: MessageDto[]
    ) {

        return [
            ...items
        ].sort(
            (
                a,
                b
            ) =>
                new Date(
                    a.createdAt
                ).getTime()

                -

                new Date(
                    b.createdAt
                ).getTime()
        );

    }

    useEffect(() => {

        loadMessages();

        const token =
            localStorage.getItem(
                "accessToken"
            );

        if (
            token
        ) {

            connectSocket(
                token,
                subscribeMessages
            );

        }

        return () => {

            disconnectSocket();

        };

    }, [chatId]);

    async function loadMessages() {

        try {

            setLoading(
                true
            );

            const res =
                await api.get(
                    `/chats/${chatId}/messages`
                );

            const loaded =
                Array.isArray(
                    res.data
                )
                    ? res.data
                    : [];

            setMessages(
                sortMessages(
                    loaded
                )
            );

            await markMessagesAsRead(
                loaded
            );

        } catch (e) {

            console.error(
                e
            );

        } finally {

            setLoading(
                false
            );

        }

    }

    async function markMessagesAsRead(
        loaded: MessageDto[]
    ) {

        if (
            !user
        ) {
            return;
        }

        const unread =
            loaded.filter(
                m =>
                    m.sender.id !==
                    user.id &&
                    m.status ===
                    "SENT"
            );

        try {

            await Promise.all(

                unread.map(
                    m =>
                        api.patch(
                            `/chats/${chatId}/messages/${m.id}/read`
                        )
                )

            );

            setMessages(
                prev =>

                    prev.map(
                        m => {

                            const exists =
                                unread.some(
                                    x =>
                                        x.id ===
                                        m.id
                                );

                            if (
                                !exists
                            ) {
                                return m;
                            }

                            return {

                                ...m,

                                status:
                                    "READ"

                            };

                        }
                    )

            );

        } catch (e) {

            console.error(
                e
            );

        }

    }

    function subscribeMessages() {

        const socket =
            getSocket();

        if (
            !socket
        ) {
            return;
        }

        socket.subscribe(

            `/topic/chat/${chatId}`,

            frame => {

                const message =
                    JSON.parse(
                        frame.body
                    );

                setMessages(
                    prev => {

                        if (

                            prev.some(
                                x =>
                                    x.id ===
                                    message.id
                            )

                        ) {

                            return prev;

                        }

                        return sortMessages([
                            ...prev,
                            message
                        ]);

                    }
                );

            },

            {
                id:
                    `chat-${chatId}`
            }

        );

    }

    function sendMessage() {

        const content =
            text.trim();

        if (
            !content
        ) {
            return;
        }

        const socket =
            getSocket();

        if (
            !socket?.connected
        ) {
            return;
        }

        socket.publish({

            destination:
                "/app/chat.send",

            body:
                JSON.stringify({

                    chatId:
                        Number(
                            chatId
                        ),

                    content

                })

        });

        setText("");

    }

    return (

        <div
            style={{
                padding:
                    20
            }}
        >

            <button
                onClick={() =>
                    navigate(
                        "/chats"
                    )
                }
            >
                ← Back
            </button>

            <h2>
                Chat {chatId}
            </h2>

            <div
                style={{
                    border:
                        "1px solid #ddd",

                    minHeight:
                        500,

                    padding:
                        16,

                    marginBottom:
                        20
                }}
            >

                {
                    loading &&
                    <p>
                        Loading...
                    </p>
                }

                {
                    messages.map(
                        m => (

                            <div
                                key={
                                    m.id
                                }
                                style={{
                                    marginBottom:
                                        12
                                }}
                            >

                                <b>
                                    {
                                        m.sender
                                            .username
                                    }
                                </b>

                                {" · "}

                                {
                                    m.status
                                }

                                <div>
                                    {
                                        m.content
                                    }
                                </div>

                                <small>

                                    {
                                        new Date(
                                            m.createdAt
                                        )
                                            .toLocaleString()
                                    }

                                </small>

                            </div>

                        )
                    )
                }

            </div>

            <div
                style={{
                    display:
                        "flex",

                    gap:
                        10
                }}
            >

                <input
                    value={
                        text
                    }
                    onChange={
                        e =>
                            setText(
                                e.target
                                    .value
                            )
                    }
                    placeholder="Type message..."
                    style={{
                        flex:
                            1
                    }}
                />

                <button
                    onClick={
                        sendMessage
                    }
                >
                    Send
                </button>

            </div>

        </div>

    );

}