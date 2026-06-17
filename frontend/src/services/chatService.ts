import { api } from "../api/client";

export async function getChats() {
    const response = await api.get("/chats");
    return response.data;
}