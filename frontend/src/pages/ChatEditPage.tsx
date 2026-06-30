import {
    useEffect,
    useMemo,
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

type UserDto = {
    id: number;
    username: string;
    role: string;
};

type ChatMemberDto = {
    user: UserDto;
    chatRole: "ADMIN" | "MEMBER";
    joinedAt: string;
};

type ChatDto = {
    id: number;
    name?: string;
    type: "PRIVATE" | "GROUP";
};

export default function ChatEditPage() {

    const { chatId } = useParams();
    const navigate = useNavigate();

    const [chat, setChat] = useState<ChatDto | null>(null);
    const [members, setMembers] = useState<ChatMemberDto[]>([]);
    const [loading, setLoading] = useState(true);
    const [currentUserId, setCurrentUserId] = useState<number | null>(null);

    useEffect(() => {

        const token = localStorage.getItem("accessToken");

        if (token) {
            connectSocket(token, () => {
                subscribe();
            });
        }

        load();

        return () => {
            disconnectSocket();
        };

    }, [chatId]);

    function subscribe() {

        const socket = getSocket();
        if (!socket) return;

        socket.subscribe(
            `/topic/chat/${chatId}`,
            frame => {

                const incoming: any = JSON.parse(frame.body);

                if (incoming?.type === "CHAT_DELETED") {
                    navigate("/chats", { replace: true });
                    return;
                }

            },
            { id: `chat-edit-${chatId}` }
        );
    }

    async function load() {

        try {

            setLoading(true);

            const [
                chatResponse,
                membersResponse,
                meResponse
            ] = await Promise.all([
                api.get(`/chats/${chatId}`),
                api.get(`/chats/${chatId}/members`),
                api.get("/users/me")
            ]);

            setChat(chatResponse.data);

            setMembers(
                Array.isArray(membersResponse.data)
                    ? membersResponse.data
                    : []
            );

            setCurrentUserId(meResponse.data.id);

        } catch (e: any) {

            if (e.response?.status === 404) {
                navigate("/chats", { replace: true });
                return;
            }

            console.error(e);

        } finally {
            setLoading(false);
        }

    }

    const currentMember = useMemo(
        () =>
            members.find(
                m => m.user.id === currentUserId
            ),
        [members, currentUserId]
    );

    const isAdmin = currentMember?.chatRole === "ADMIN";

    const canDeleteChat =
        chat?.type === "PRIVATE" ||
        (chat?.type === "GROUP" && isAdmin);

    const canRemoveMembers =
        chat?.type === "GROUP" && isAdmin;

    async function goBack() {

        // ADMIN → как раньше
        if (isAdmin) {
            navigate(`/chats/${chatId}`);
            return;
        }

        // MEMBER → проверка существования чата
        try {

            await api.get(`/chats/${chatId}`);

            navigate(`/chats/${chatId}`);

        } catch {

            navigate("/chats", { replace: true });

        }
    }

    async function removeMember(userId: number) {

        try {

            await api.delete(
                `/chats/${chatId}/members/${userId}`
            );

            setMembers(prev =>
                prev.filter(m => m.user.id !== userId)
            );

        } catch (e) {
            console.error(e);
        }

    }

    async function deleteChat() {

        if (!window.confirm("Delete this chat?")) return;

        try {

            await api.delete(`/chats/${chatId}`);

            navigate("/chats", { replace: true });

        } catch (e) {

            console.error(e);
            alert("Cannot delete chat");

        }

    }

    return (
        <div style={{ padding: 20 }}>

            <div style={{
                display: "flex",
                justifyContent: "space-between",
                alignItems: "center",
                marginBottom: 20
            }}>

                <button onClick={goBack}>
                    ← Back
                </button>

                <h2>
                    {chat?.type === "PRIVATE"
                        ? "Private Chat"
                        : "Group Settings"}
                </h2>

                {canDeleteChat && (
                    <button
                        onClick={deleteChat}
                        style={{
                            background: "#d33",
                            color: "white",
                            border: "none",
                            padding: "10px 14px",
                            borderRadius: 8,
                            cursor: "pointer"
                        }}
                    >
                        Delete chat
                    </button>
                )}

            </div>

            {loading && <p>Loading...</p>}

            {!loading && members.length === 0 && (
                <p>No members</p>
            )}

            <div style={{
                display: "flex",
                flexDirection: "column",
                gap: 12
            }}>

                {members.map(member => {

                    const canRemoveThisMember =
                        canRemoveMembers &&
                        member.chatRole !== "ADMIN" &&
                        member.user.id !== currentUserId;

                    return (
                        <div
                            key={member.user.id}
                            style={{
                                display: "flex",
                                justifyContent: "space-between",
                                alignItems: "center",
                                padding: 14,
                                border: "1px solid #ddd",
                                borderRadius: 8
                            }}
                        >

                            <div>
                                <b>{member.user.username}</b>

                                <div>
                                    Role: {member.chatRole}
                                </div>

                                <small>
                                    Joined:{" "}
                                    {new Date(member.joinedAt).toLocaleString()}
                                </small>
                            </div>

                            {canRemoveThisMember && (
                                <button
                                    onClick={() =>
                                        removeMember(member.user.id)
                                    }
                                >
                                    Remove
                                </button>
                            )}

                        </div>
                    );
                })}

            </div>

        </div>
    );
}