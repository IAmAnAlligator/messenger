import { api } from "../api/client";
import type { ChatDto } from "../types/chat";


export async function getChat(
    chatId: string
): Promise<ChatDto> {

    const response =
        await api.get<ChatDto>(
            `/chats/${chatId}`
        );

    return response.data;
}