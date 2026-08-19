import { api } from "../api/client";

import type {
    MessageDto
} from "../types/message";

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

export async function sendFile(
    chatId: number,
    file: File
): Promise<MessageDto> {

    const formData =
        new FormData();

    formData.append(
        "file",
        file
    );

    const response =
        await api.post<MessageDto>(
            `/chats/${chatId}/messages/file`,
            formData
        );

    return response.data;
}

export async function getMessageFile(
    chatId: number,
    messageId: number
): Promise<Blob> {

    const response =
        await api.get(
            `/chats/${chatId}/messages/${messageId}/file`,
            {
                responseType: "blob"
            }
        );

    return response.data;
}