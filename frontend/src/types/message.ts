import type { UserDto } from "./user";
import type { CursorPageResponse } from "./pagination";


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
    attachment: FileAttachmentDto | null;
}


export type MessagePageDto =
    CursorPageResponse<MessageDto>;