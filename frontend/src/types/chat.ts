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
}


export interface ChatDto {

    id: number;

    name: string | null;

    type: ChatType;

    members: ChatMemberDto[];

    createdAt: string;

    lastMessageAt: string;
}