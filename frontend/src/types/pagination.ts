import type { MessageId } from "./ids";


export type CursorDto = {

    cursorTime: string;

    cursorId: MessageId;

};


export type CursorPageResponse<T> = {

    content: T[];

    nextCursor: CursorDto | null;

    hasNext: boolean;

};