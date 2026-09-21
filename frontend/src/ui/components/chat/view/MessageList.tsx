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

    currentUserId: number;

    isMessageReadByOtherUser(
        messageId: number
    ): boolean;

    onDelete(
        id: number
    ): void;

    onLoadMore():
        void | Promise<void>;

    onReadUpTo(
        messageId: number
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
        useRef<number | null>(null);

    const previousScrollHeight =
        useRef(0);

    const previousMessagesLength =
        useRef(0);

    const loadingMoreRef =
        useRef(false);

    const lastSentReadMessageId =
        useRef<number | null>(null);


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


        let lastVisibleIncomingId:
            number | null = null;


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
                Number(
                    element.dataset.messageId
                );


            if (Number.isNaN(id)) {
                return;
            }


            const message =
                messages.find(
                    item =>
                        item.id === id
                );


            if (!message) {
                return;
            }


            if (
                message.sender.id ===
                currentUserId
            ) {
                return;
            }


            if (
                lastVisibleIncomingId === null ||
                id > lastVisibleIncomingId
            ) {

                lastVisibleIncomingId =
                    id;

            }

        });


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


        if (
            lastSentReadMessageId.current !== null &&
            lastVisibleIncomingId <=
                lastSentReadMessageId.current
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