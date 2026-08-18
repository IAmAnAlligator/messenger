import type { UserDto } from "./user";
import type { CursorPageResponse } from "./pagination";


export type MessageStatus =
    | "SENT"
    | "READ";


export interface FileAttachmentDto {
    id: string;
    originalFileName: string;
    contentType: string;
    size: number;
    url: string;
}

export interface MessageDto {
    id: number;
    chatId: number;
    sender: UserDto;
    content: string | null;
    createdAt: string;
    status: MessageStatus;
    attachment: FileAttachmentDto | null;
}


export type MessagePageDto =
    CursorPageResponse<MessageDto>;