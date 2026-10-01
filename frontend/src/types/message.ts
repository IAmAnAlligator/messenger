import type {
    ChatId,
    FileAttachmentId,
    MessageId
} from "./ids";

import type { UserDto } from "./user";

import type { CursorPageResponse } from "./pagination";


export interface FileAttachmentDto {

    id: FileAttachmentId;

    originalFileName: string;

    contentType: string;

    size: number;

    url: string;

}


export interface MessageDto {

    id: MessageId;

    chatId: ChatId;

    sender: UserDto;

    content: string | null;

    createdAt: string;

    attachment: FileAttachmentDto | null;

}


export type MessagePageDto =
    CursorPageResponse<MessageDto>;