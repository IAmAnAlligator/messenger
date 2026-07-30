import { api } from "./client";
import type { MessageDto } from "./dto/message";

export async function getMessages(
    chatId: number,
    cursor?: number
): Promise<MessageDto[]> {

    const response = await api.get<MessageDto[]>(
        `/chats/${chatId}/messages`,
        {
            params: {
                cursor
            }
        }
    );

    return response.data;
}