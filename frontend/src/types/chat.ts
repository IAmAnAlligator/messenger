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

    lastReadMessageId: number | null;

}



export interface ChatDto {

    id: number;

    name: string | null;

    type: ChatType;

    members: ChatMemberDto[];

    createdAt: string;

    lastMessageAt: string | null;

}

export interface ChatMemberReadDto {
    userId: number;
    lastReadMessageId: number | null;
}

export interface MessageReadEvent {

    messageId: number;

    chatId: number;

    readerId: number;

    readAt: string;

    lastReadMessageId: number;

}