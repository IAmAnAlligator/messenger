import type { UserDto } from "./user";


export type MessageStatus =
    | "SENT"
    | "READ";


export interface MessageDto {

    id: number;

    chatId: number;

    sender: UserDto;

    content: string;

    createdAt: string;

    status: MessageStatus;
}