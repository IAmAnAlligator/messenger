import { api } from "../api/client";

import type {
    MessageDto
} from "../types/message";


export type MessagePageDto = {

    items: MessageDto[];

    nextCursor: number | null;

    hasMore: boolean;

};



export async function getMessages(

    chatId: number,

    cursor?: number,

    limit: number = 50

): Promise<MessagePageDto> {


    const response =
        await api.get<MessagePageDto>(
            `/chats/${chatId}/messages`,
            {
                params: {

                    cursor,

                    limit

                }
            }
        );


    return response.data;

}