import type { MessageDto } from "./message";

import type {
    ChatId,
    MessageId,
    UserId
} from "./ids";

import type {
    ChatType,
    MessageReadEvent
} from "./chat";


/**
 * Ошибка WebSocket validation.
 *
 * Backend:
 * @SendToUser("/queue/errors")
 */
export interface WebSocketErrorResponse {

    message: string;

}


/**
 * CHAT CREATED
 */
export interface ChatCreatedSocketEvent {

    type: "CHAT_CREATED";

    payload: {

        chatId: ChatId;

        name: string;

        type: ChatType;

        memberIds: UserId[];

    };

}


/**
 * CHAT DELETED
 */
export interface ChatDeletedSocketEvent {

    type: "CHAT_DELETED";

    payload: {

        chatId: ChatId;

    };

}


/**
 * CHAT RENAMED
 */
export interface ChatRenamedSocketEvent {

    type: "CHAT_RENAMED";

    payload: {

        chatId: ChatId;

        name: string;

    };

}


/**
 * MEMBER ADDED
 */
export interface ChatMemberAddedSocketEvent {

    type: "CHAT_MEMBER_ADDED";

    payload: {

        chatId: ChatId;

        userId: UserId;

    };

}


/**
 * MEMBER REMOVED
 */
export interface ChatMemberRemovedSocketEvent {

    type: "CHAT_MEMBER_REMOVED";

    payload: {

        chatId: ChatId;

        userId: UserId;

    };

}


/**
 * MEMBER LEFT
 */
export interface ChatMemberLeftSocketEvent {

    type: "CHAT_MEMBER_LEFT";

    payload: {

        chatId: ChatId;

        userId: UserId;

    };

}


/**
 * MESSAGE CREATED
 */
export interface MessageSentSocketEvent {

    type: "MESSAGE_CREATED";

    payload: MessageDto;

}


/**
 * MESSAGE READ
 */
export interface MessageReadSocketEvent {

    type: "MESSAGE_READ";

    payload: MessageReadEvent;

}


/**
 * MESSAGE DELETED
 */
export interface MessageDeletedSocketEvent {

    type: "MESSAGE_DELETED";

    payload: {

        chatId: ChatId;

        messageId: MessageId;

    };

}


/**
 * Все WebSocket события.
 */
export type ChatSocketEvent =
    | ChatCreatedSocketEvent
    | ChatDeletedSocketEvent
    | ChatRenamedSocketEvent
    | ChatMemberAddedSocketEvent
    | ChatMemberRemovedSocketEvent
    | ChatMemberLeftSocketEvent
    | MessageSentSocketEvent
    | MessageReadSocketEvent
    | MessageDeletedSocketEvent;