import { api } from "../api/client";


import type {
    MessagePageDto
} from "../types/message";


import type {
    CursorDto
} from "../types/pagination";




export async function getMessages(

    chatId: number,

    cursor?: CursorDto,

    limit: number = 50

): Promise<MessagePageDto> {


    const response =
        await api.get<MessagePageDto>(
            `/chats/${chatId}/messages`,
            {
                params: {

                    cursorTime:
                        cursor?.cursorTime,


                    cursorId:
                        cursor?.cursorId,


                    limit

                }
            }
        );


    return response.data;

}