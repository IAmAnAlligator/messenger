import {
    useCallback,
    useEffect,
    useLayoutEffect,
    useRef,
    useState
} from "react";

import MessageItem from "./MessageItem";

import type {
    MessageDto
} from "../../../../types/message";

import "../../../styles/chatPage.css";

type Props = {
    loading: boolean;
    loadingMore: boolean;
    hasMore: boolean;
    messages: MessageDto[];
    currentUserId: string;
    currentUserLastReadMessageId: string | null;
    isMessageReadByOtherUser(messageId: string): boolean;
    onDelete(id: string): void;
    onUserProfile(userId: string): void;
    onLoadMore(): void | Promise<void>;
    onReadUpTo(messageId: string): void;
};

const BOTTOM_THRESHOLD = 100;

export default function MessageList({
    loading,
    loadingMore,
    hasMore,
    messages,
    currentUserId,
    currentUserLastReadMessageId,
    isMessageReadByOtherUser,
    onDelete,
    onUserProfile,
    onLoadMore,
    onReadUpTo
}: Props) {
    const containerRef = useRef<HTMLDivElement>(null);

    const initialized = useRef(false);

    const previousLastMessageId = useRef<string | null>(null);

    const isNearBottomRef = useRef(true);

    const previousScrollHeight = useRef(0);
    const previousScrollTop = useRef(0);
    const previousMessagesLength = useRef(0);

    const loadingMoreRef = useRef(false);

    const lastSentReadMessageId = useRef<string | null>(
        currentUserLastReadMessageId
    );

    const [newMessagesCount, setNewMessagesCount] = useState(0);

    // --------------------------------------------------
    // Scroll helpers
    // --------------------------------------------------

    const updateNearBottom = useCallback(() => {
        const container = containerRef.current;

        if (!container) return;

        const distanceFromBottom =
            container.scrollHeight -
            container.clientHeight -
            container.scrollTop;

        isNearBottomRef.current =
            distanceFromBottom <= BOTTOM_THRESHOLD;

        if (isNearBottomRef.current) {
            setNewMessagesCount(0);
        }
    }, []);

    const scrollToBottom = useCallback(
        (behavior: ScrollBehavior = "smooth") => {
            const container = containerRef.current;

            if (!container) return;

            container.scrollTo({
                top: container.scrollHeight,
                behavior
            });

            isNearBottomRef.current = true;
            setNewMessagesCount(0);
        },
        []
    );

    const scrollToMessage = useCallback((messageId: string): boolean => {
        const container = containerRef.current;

        if (!container) return false;

        const target = Array.from(
            container.querySelectorAll<HTMLElement>(
                "[data-message-id]"
            )
        ).find(element => element.dataset.messageId === messageId);

        if (!target) return false;

        const containerRect = container.getBoundingClientRect();
        const targetRect = target.getBoundingClientRect();

        container.scrollTop += targetRect.top - containerRect.top;

        return true;
    }, []);

    // --------------------------------------------------
    // Read messages
    // --------------------------------------------------

    const markVisibleMessagesAsRead = useCallback(() => {
        const container = containerRef.current;

        if (!container || messages.length === 0) return;

        const containerRect = container.getBoundingClientRect();

        const elements = container.querySelectorAll<HTMLElement>(
            "[data-message-id]"
        );

        const messageIndexes = new Map<string, number>();

        messages.forEach((message, index) => {
            messageIndexes.set(message.id, index);
        });

        let lastVisibleIncomingIndex = -1;

        elements.forEach(element => {
            const rect = element.getBoundingClientRect();

            const isVisible =
                rect.bottom > containerRect.top &&
                rect.top < containerRect.bottom;

            if (!isVisible) return;

            const id = element.dataset.messageId;

            if (!id) return;

            const messageIndex = messageIndexes.get(id);

            if (messageIndex === undefined) return;

            const message = messages[messageIndex];

            if (message.sender.id === currentUserId) return;

            if (messageIndex > lastVisibleIncomingIndex) {
                lastVisibleIncomingIndex = messageIndex;
            }
        });

        if (lastVisibleIncomingIndex === -1) return;

        const lastVisibleIncomingId =
            messages[lastVisibleIncomingIndex].id;

        const lastSentIndex =
            lastSentReadMessageId.current === null
                ? -1
                : messageIndexes.get(
                    lastSentReadMessageId.current
                ) ?? -1;

        if (
            lastSentIndex !== -1 &&
            lastVisibleIncomingIndex <= lastSentIndex
        ) {
            return;
        }

        lastSentReadMessageId.current = lastVisibleIncomingId;

        onReadUpTo(lastVisibleIncomingId);
    }, [
        messages,
        currentUserId,
        onReadUpTo
    ]);

    // --------------------------------------------------
    // Initial positioning and message updates
    // --------------------------------------------------

    useLayoutEffect(() => {
        if (loading) return;

        const container = containerRef.current;

        if (!container) return;

        const lastMessage =
            messages.length > 0
                ? messages[messages.length - 1]
                : null;

        // Initial positioning.
        if (!initialized.current) {


console.log("Initial chat scroll:", {
    lastReadMessageId: currentUserLastReadMessageId,
    firstLoadedMessageId: messages[0]?.id,
    lastLoadedMessageId: messages[messages.length - 1]?.id,
    loadedMessagesCount: messages.length,
    lastReadMessageIndex: messages.findIndex(
        message => message.id === currentUserLastReadMessageId
    )
});

            const lastReadIndex = currentUserLastReadMessageId
                ? messages.findIndex(
                    message =>
                        message.id === currentUserLastReadMessageId
                )
                : -1;

            const hasUnreadMessages = currentUserLastReadMessageId
                ? lastReadIndex === -1
                    ? messages.some(
                        message => message.sender.id !== currentUserId
                    )
                    : messages
                        .slice(lastReadIndex + 1)
                        .some(
                            message => message.sender.id !== currentUserId
                        )
                : messages.some(
                    message => message.sender.id !== currentUserId
                );

            if (hasUnreadMessages) {
                if (lastReadIndex !== -1) {
                    scrollToMessage(currentUserLastReadMessageId!);
                } else {
                    // The read cursor is outside the loaded page.
                    container.scrollTop = 0;
                }
            } else {
                // No unread incoming messages.
                container.scrollTop = container.scrollHeight;
            }

            initialized.current = true;

            previousLastMessageId.current =
                lastMessage?.id ?? null;

            requestAnimationFrame(() => {
                updateNearBottom();
                markVisibleMessagesAsRead();
            });

            return;
        }

        // Preserve position when older messages are loaded.
        if (
            loadingMoreRef.current &&
            messages.length > previousMessagesLength.current
        ) {
            const difference =
                container.scrollHeight - previousScrollHeight.current;

            container.scrollTop =
                previousScrollTop.current + difference;

            loadingMoreRef.current = false;

            previousLastMessageId.current =
                lastMessage?.id ?? null;

            requestAnimationFrame(() => {
                updateNearBottom();
                markVisibleMessagesAsRead();
            });

            return;
        }

        if (!lastMessage) {
            previousLastMessageId.current = null;
            return;
        }

        const previousLastIndex =
            previousLastMessageId.current === null
                ? -1
                : messages.findIndex(
                    message =>
                        message.id === previousLastMessageId.current
                );

        const hasNewMessages =
            previousLastMessageId.current !== null &&
            previousLastIndex !== -1 &&
            previousLastIndex < messages.length - 1;

        if (hasNewMessages) {
            const appendedMessages = messages.slice(
                previousLastIndex + 1
            );

            const incomingMessagesCount = appendedMessages.filter(
                message => message.sender.id !== currentUserId
            ).length;

            if (isNearBottomRef.current) {
                scrollToBottom();
            } else if (incomingMessagesCount > 0) {
                setNewMessagesCount(
                    count => count + incomingMessagesCount
                );
            }
        }

        previousLastMessageId.current = lastMessage.id;

        requestAnimationFrame(() => {
            markVisibleMessagesAsRead();
        });
    }, [
        loading,
        messages,
        currentUserLastReadMessageId,
        currentUserId,
        scrollToMessage,
        scrollToBottom,
        updateNearBottom,
        markVisibleMessagesAsRead
    ]);

    // --------------------------------------------------
    // Loading older messages
    // --------------------------------------------------

    useEffect(() => {
        if (
            !loadingMore &&
            loadingMoreRef.current &&
            messages.length <= previousMessagesLength.current
        ) {
            loadingMoreRef.current = false;
        }
    }, [
        loadingMore,
        messages.length
    ]);

    // --------------------------------------------------
    // Scroll event
    // --------------------------------------------------

    useEffect(() => {
        const container = containerRef.current;

        if (!container) return;

        const handleScroll = () => {
            updateNearBottom();

            if (
                container.scrollTop <= 20 &&
                hasMore &&
                !loadingMore &&
                !loadingMoreRef.current &&
                messages.length > 0
            ) {
                previousScrollHeight.current =
                    container.scrollHeight;

                previousScrollTop.current =
                    container.scrollTop;

                previousMessagesLength.current =
                    messages.length;

                loadingMoreRef.current = true;

                void onLoadMore();
            }

            markVisibleMessagesAsRead();
        };

        container.addEventListener(
            "scroll",
            handleScroll,
            { passive: true }
        );

        return () => {
            container.removeEventListener(
                "scroll",
                handleScroll
            );
        };
    }, [
        hasMore,
        loadingMore,
        messages.length,
        onLoadMore,
        updateNearBottom,
        markVisibleMessagesAsRead
    ]);

    // --------------------------------------------------
    // Render
    // --------------------------------------------------

    if (loading) {
        return <div className="messages">Loading...</div>;
    }

    return (
        <div className="messages" ref={containerRef}>
            {loadingMore && hasMore && (
                <div className="messages-loader">
                    Loading...
                </div>
            )}

            {messages.map(message => {
                const isOwnMessage =
                    message.sender.id === currentUserId;

                const isRead =
                    isOwnMessage &&
                    isMessageReadByOtherUser(message.id);

                return (
                    <div
                        key={message.id}
                        data-message-id={message.id}
                    >
                        <MessageItem
                            message={message}
                            onDelete={onDelete}
                            onUserProfile={onUserProfile}
                            isRead={isRead}
                        />
                    </div>
                );
            })}

            {newMessagesCount > 0 && (
                <button
                    type="button"
                    className="new-messages-button"
                    onClick={() => scrollToBottom()}
                >
                    {newMessagesCount} new{" "}
                    {newMessagesCount === 1
                        ? "message"
                        : "messages"}{" "}
                    ↓
                </button>
            )}
        </div>
    );
}