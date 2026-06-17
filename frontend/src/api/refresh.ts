import { api } from "./client";

export async function refreshToken() {

    const refresh = localStorage.getItem("refreshToken");

    const response = await api.post("/auth/refresh", {
        refreshToken: refresh
    });

    return response.data;
}