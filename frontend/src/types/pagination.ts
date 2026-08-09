export type CursorDto = {

    cursorTime: string;

    cursorId: number;

};


export type CursorPageResponse<T> = {

    content: T[];

    nextCursor: CursorDto | null;

    hasNext: boolean;

};