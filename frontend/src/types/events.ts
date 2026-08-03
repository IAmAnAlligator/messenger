import type { MessageDto } from "./message";


/**
 * Ошибки WebSocket validation
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

        chatId: number;

        name: string;

        type: "PRIVATE" | "GROUP";

        memberIds: number[];

    };

}



/**
 * CHAT DELETED
 */
export interface ChatDeletedSocketEvent {

    type: "CHAT_DELETED";

    payload: {

        chatId: number;

    };

}



/**
 * CHAT RENAMED
 */
export interface ChatRenamedSocketEvent {

    type: "CHAT_RENAMED";

    payload: {

        chatId: number;

        name: string;

    };

}



/**
 * MEMBER ADDED
 */
export interface ChatMemberAddedSocketEvent {

    type: "CHAT_MEMBER_ADDED";

    payload: {

        chatId: number;

        userId: number;

    };

}



/**
 * MEMBER REMOVED
 */
export interface ChatMemberRemovedSocketEvent {

    type: "CHAT_MEMBER_REMOVED";

    payload: {

        chatId: number;

        userId: number;

    };

}



/**
 * MEMBER LEFT
 */
export interface ChatMemberLeftSocketEvent {

    type: "CHAT_MEMBER_LEFT";

    payload: {

        chatId: number;

        userId: number;

    };

}



/**
 * MESSAGE SENT
 *
 * Backend:
 * WebSocketEvent.of(
 *     EventType.MESSAGE_SENT,
 *     dto
 * )
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

    payload: {

        chatId: number;

        messageId: number;

    };

}



/**
 * MESSAGE DELETED
 */
export interface MessageDeletedSocketEvent {

    type: "MESSAGE_DELETED";

    payload: {

        chatId: number;

        messageId: number;

    };

}



/**
 * Все WebSocket события
 *
 * Используется:
 *
 * const event =
 * JSON.parse(frame.body)
 * as ChatSocketEvent;
 *
 * switch(event.type)
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