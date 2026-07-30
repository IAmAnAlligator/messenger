import { api } from "./client";
import type { ChatDto } from "./dto/chat";


export async function getChat(
    chatId: number
): Promise<ChatDto> {

    const response =
        await api.get<ChatDto>(
            `/chats/${chatId}`
        );

    return response.data;
}