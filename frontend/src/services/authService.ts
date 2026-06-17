import { api } from "../api/client";

import type {
    LoginRequest,
    RegisterRequest,
    AuthResponse
} from "../types/auth";

export async function login(
    data: LoginRequest
): Promise<AuthResponse> {

    const response =
        await api.post(
            "/auth/login",
            data
        );

    return response.data;
}

export async function register(
    data: RegisterRequest
): Promise<AuthResponse> {

    const response =
        await api.post(
            "/auth/register",
            data
        );

    return response.data;
}