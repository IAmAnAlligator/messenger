import {
    useEffect,
    useLayoutEffect,
    useRef
} from "react";

import MessageItem
    from "./MessageItem";

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

    isMessageReadByOtherUser(
        messageId: string
    ): boolean;

    onDelete(
        id: string
    ): void;

    onUserProfile(
         userId: string
    ): void;

    onLoadMore():
        void | Promise<void>;

    onReadUpTo(
        messageId: string
    ): void;

};


export default function MessageList({

    loading,

    loadingMore,

    hasMore,

    messages,

    currentUserId,

    isMessageReadByOtherUser,

    onDelete,

    onUserProfile,

    onLoadMore,

    onReadUpTo

}: Props) {

    const containerRef =
        useRef<HTMLDivElement>(null);

    const bottomRef =
        useRef<HTMLDivElement>(null);

    const initialized =
        useRef(false);

    const previousLastMessageId =
        useRef<string | null>(null);

    const previousScrollHeight =
        useRef(0);

    const previousMessagesLength =
        useRef(0);

    const loadingMoreRef =
        useRef(false);

    const lastSentReadMessageId =
        useRef<string | null>(null);


    function markVisibleMessagesAsRead() {

        const container =
            containerRef.current;


        if (
            !container ||
            messages.length === 0
        ) {
            return;
        }


        const containerRect =
            container.getBoundingClientRect();


        const elements =
            container.querySelectorAll<HTMLElement>(
                "[data-message-id]"
            );


        let lastVisibleIncomingIndex =
            -1;


        elements.forEach(element => {

            const rect =
                element.getBoundingClientRect();


            const visible =
                rect.bottom >
                    containerRect.top &&
                rect.top <
                    containerRect.bottom;


            if (!visible) {
                return;
            }


            const id =
                element.dataset.messageId;


            if (!id) {
                return;
            }


            const messageIndex =
                messages.findIndex(
                    item =>
                        item.id === id
                );


            if (messageIndex === -1) {
                return;
            }


            const message =
                messages[messageIndex];


            if (
                message.sender.id ===
                currentUserId
            ) {
                return;
            }


            /*
             * Не сравниваем UUID как числа.
             *
             * messages уже находятся в порядке,
             * заданном backend.
             *
             * Поэтому выбираем сообщение
             * с максимальным индексом.
             */

            if (
                messageIndex >
                lastVisibleIncomingIndex
            ) {

                lastVisibleIncomingIndex =
                    messageIndex;

            }

        });


        const lastVisibleIncomingId =
            lastVisibleIncomingIndex >= 0
                ? messages[
                    lastVisibleIncomingIndex
                ].id
                : null;


        console.log(
            "[READ] visible check:",
            {
                messages:
                    messages.map(
                        message => message.id
                    ),

                elements:
                    elements.length,

                lastVisibleIncomingId
            }
        );


        if (
            lastVisibleIncomingId === null
        ) {
            return;
        }


        /*
         * Повторно отправлять тот же read
         * не нужно.
         *
         * Если старый messageId всё ещё
         * присутствует в текущем массиве,
         * сравниваем позиции сообщений,
         * а не UUID.
         */

        const lastSentIndex =
            lastSentReadMessageId.current === null
                ? -1
                : messages.findIndex(
                    message =>
                        message.id ===
                        lastSentReadMessageId.current
                );


        if (
            lastSentIndex !== -1 &&
            lastVisibleIncomingIndex <=
                lastSentIndex
        ) {
            return;
        }


        lastSentReadMessageId.current =
            lastVisibleIncomingId;


        console.log(
            "[READ] mark incoming as read:",
            lastVisibleIncomingId
        );


        onReadUpTo(
            lastVisibleIncomingId
        );

    }


    useLayoutEffect(() => {

        if (
            loading ||
            messages.length === 0
        ) {
            return;
        }


        const container =
            containerRef.current;


        if (!container) {
            return;
        }


        bottomRef.current?.scrollIntoView();


        initialized.current = true;


        previousLastMessageId.current =
            messages[
                messages.length - 1
            ].id;


        requestAnimationFrame(() => {

            markVisibleMessagesAsRead();

        });

    }, [
        loading,
        messages
    ]);


    useEffect(() => {

        if (
            !initialized.current ||
            messages.length === 0
        ) {
            return;
        }


        const lastMessage =
            messages[
                messages.length - 1
            ];


        const isNewMessage =
            previousLastMessageId.current !== null &&
            lastMessage.id !==
                previousLastMessageId.current;


        if (
            isNewMessage &&
            !loadingMoreRef.current
        ) {

            bottomRef.current?.scrollIntoView({
                behavior: "smooth"
            });


            if (
                lastMessage.sender.id !==
                currentUserId
            ) {

                requestAnimationFrame(() => {

                    markVisibleMessagesAsRead();

                });

            }

        }


        previousLastMessageId.current =
            lastMessage.id;


    }, [
        messages,
        currentUserId
    ]);


    useEffect(() => {

        const container =
            containerRef.current;


        if (
            !container ||
            !loadingMoreRef.current
        ) {
            return;
        }


        const addedMessages =
            messages.length >
            previousMessagesLength.current;


        if (addedMessages) {

            const newScrollHeight =
                container.scrollHeight;


            requestAnimationFrame(() => {

                container.scrollTop =
                    newScrollHeight -
                    previousScrollHeight.current;


                loadingMoreRef.current =
                    false;

            });

        } else {

            loadingMoreRef.current =
                false;

        }

    }, [
        messages
    ]);


    useEffect(() => {

        const container =
            containerRef.current;


        if (!container) {
            return;
        }


        const handleScroll = () => {

            if (
                container.scrollTop <= 20 &&
                hasMore &&
                !loadingMore &&
                messages.length > 0
            ) {

                previousScrollHeight.current =
                    container.scrollHeight;

                previousMessagesLength.current =
                    messages.length;

                loadingMoreRef.current =
                    true;

                onLoadMore();

            }


            markVisibleMessagesAsRead();

        };


        container.addEventListener(
            "scroll",
            handleScroll
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
        onLoadMore,
        messages,
        currentUserId
    ]);


    if (loading) {

        return (

            <div className="messages">

                Loading...

            </div>

        );

    }


    return (

        <div
            className="messages"
            ref={containerRef}
        >

            {
                loadingMore &&
                hasMore && (

                    <div className="messages-loader">

                        Loading...

                    </div>

                )
            }


            {
                messages.map(
                    message => {

                        const isOwnMessage =
                            message.sender.id ===
                            currentUserId;


                        const isRead =
                            isOwnMessage &&
                            isMessageReadByOtherUser(
                                message.id
                            );


                        console.log(
                            "[READ UI]",
                            {
                                messageId:
                                    message.id,

                                senderId:
                                    message.sender.id,

                                currentUserId,

                                isOwnMessage,

                                isRead
                            }
                        );


                        return (

                            <div
                                key={message.id}
                                data-message-id={
                                    message.id
                                }
                            >

                                <MessageItem

                                    message={
                                        message
                                    }

                                    onDelete={
                                        onDelete
                                    }

                                        onUserProfile={
        onUserProfile
    }

                                    isRead={
                                        isRead
                                    }

                                />

                            </div>

                        );

                    }
                )
            }


            <div ref={bottomRef} />

        </div>

    );

}
