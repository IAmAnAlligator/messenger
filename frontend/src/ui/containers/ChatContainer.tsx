
import {
    useEffect,
    useState
} from "react";

import {
    useNavigate
} from "react-router-dom";

import {
    useAuth
} from "../../contexts/AuthContext";

import {
    useChat
} from "../../hooks/useChat";

import {
    useChatSocket
} from "../../hooks/useChatSocket";

import ChatContent
    from "../components/chat/view/ChatContent";

import {
    onSocketConnected,
    updatePresence,
    subscribe,
    unsubscribe
} from "../../services/chatSocket";


type Props = {
    chatId?: string;
};


type PresenceStatus = {
    userId: string;
    online: boolean;
};


export default function ChatContainer(
    {
        chatId
    }: Props
) {

    const navigate =
        useNavigate();

    const { user } =
        useAuth();

    const [
        text,
        setText
    ] = useState("");

    const [
        isOnline,
        setIsOnline
    ] = useState(false);

    const {
        chat,
        messages,
        loading,
        loadingMore,
        hasMore,
        isMessageReadByOtherUser,
        addMessage,
        removeMessage,
        updateMessageStatus,
        markCurrentUserRead,
        reloadMessages,
        loadMoreMessages,
        sendFile
    } = useChat(chatId);

    const otherUserId =
        chat?.type === "PRIVATE"
            ? chat.members.find(
                member =>
                    member.user.id !== user?.id
            )?.user.id ?? null
            : null;

    const {
        error,
        sendMessage,
        deleteMessage,
        sendReadUpTo
    } = useChatSocket({
        chatId,
        onMessage: addMessage,
        onDelete: removeMessage,
        onRead: updateMessageStatus,
        reloadMessages
    });


    useEffect(() => {

        function publishCurrentPresence() {

            const visible =
                document.visibilityState === "visible";

            updatePresence(
                chatId ?? null,
                visible && Boolean(chatId)
            );

        }

        publishCurrentPresence();

        const unsubscribeConnection =
            onSocketConnected(
                publishCurrentPresence
            );

        document.addEventListener(
            "visibilitychange",
            publishCurrentPresence
        );

        return () => {

            document.removeEventListener(
                "visibilitychange",
                publishCurrentPresence
            );

            unsubscribeConnection();

            updatePresence(
                null,
                false
            );

        };

    }, [chatId]);


    useEffect(() => {

        setIsOnline(false);

        if (!otherUserId) {
            return;
        }

        const handlePresence = (
            message: { body: string }
        ) => {

            try {

                const presence =
                    JSON.parse(
                        message.body
                    ) as PresenceStatus;

                if (
                    presence.userId === otherUserId
                ) {

                    setIsOnline(
                        presence.online
                    );

                }

            } catch (error) {

                console.error(
                    "Failed to parse presence event",
                    error
                );

            }

        };

        const liveDestination =
            `/topic/presence/user/${otherUserId}`;

        const initialDestination =
            `/app/presence/user/${otherUserId}`;

        subscribe(
            liveDestination,
            handlePresence
        );

        subscribe(
            initialDestination,
            handlePresence
        );

        return () => {

            unsubscribe(
                liveDestination,
                handlePresence
            );

            unsubscribe(
                initialDestination,
                handlePresence
            );

        };

    }, [otherUserId]);


    function handleReadUpTo(
        messageId: string
    ) {

        sendReadUpTo(
            messageId
        );

        markCurrentUserRead(
            messageId
        );

    }


    async function handleSendFile(
        file: File
    ) {

        try {

            await sendFile(
                file
            );

        } catch (error) {

            console.error(
                "Failed to send file",
                error
            );

        }

    }


    function handleUserProfile(
        userId: string
    ) {

        navigate(
            `/chats/${chatId}/members/${userId}/profile`
        );

    }


    function handleSend() {

        const content =
            text.trim();

        if (!content) {
            return;
        }

        sendMessage(
            content
        );

        setText("");

    }


    if (!user) {
        return null;
    }


    return (

        <ChatContent

            chat={
                chat
            }

            messages={
                messages
            }

            loading={
                loading
            }

            loadingMore={
                loadingMore
            }

            hasMore={
                hasMore
            }

            currentUserId={
                user.id
            }

            isMessageReadByOtherUser={
                isMessageReadByOtherUser
            }

            isOnline={
                isOnline
            }

            onLoadMore={
                loadMoreMessages
            }

            onSendFile={
                handleSendFile
            }

            onReadUpTo={
                handleReadUpTo
            }

            onUserProfile={
                handleUserProfile
            }

            text={
                text
            }

            error={
                error
            }

            onTextChange={
                setText
            }

            onSend={
                handleSend
            }

            onDelete={
                deleteMessage
            }

            onBack={() =>
                navigate(
                    "/chats"
                )
            }

            onEdit={() =>
                navigate(
                    `/chats/${chatId}/edit`
                )
            }

        />

    );

}