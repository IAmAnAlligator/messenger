import type {
    ChatId,
    MessageId,
    UserId
} from "./ids";

import type { UserDto } from "./user";


export type ChatType =
    | "PRIVATE"
    | "GROUP";


export type ChatRole =
    | "ADMIN"
    | "MEMBER";


export interface ChatMemberDto {

    user: UserDto;

    chatRole: ChatRole;

    joinedAt: string;

    lastReadMessageId: MessageId | null;

}


export interface ChatDto {

    id: ChatId;

    name: string | null;

    type: ChatType;

    members: ChatMemberDto[];

    createdAt: string;

    lastMessageAt: string | null;

}


export interface ChatMemberReadDto {

    userId: UserId;

    lastReadMessageId: MessageId | null;

}


export interface MessageReadEvent {

    messageId: MessageId;

    chatId: ChatId;

    readerId: UserId;

    readAt: string;

    lastReadMessageId: MessageId;

}