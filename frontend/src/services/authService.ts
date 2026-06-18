import { api } from "../api/client";

/**
 * 📦 Типы запросов/ответов авторизации
 * вынесены отдельно, чтобы:
 * - не дублировать структуры
 * - легче поддерживать контракт с backend
 */
import type {
    LoginRequest,
    RegisterRequest,
    AuthResponse
} from "../types/auth";

/**
 * 🔐 LOGIN REQUEST
 *
 * Отвечает только за HTTP-запрос на backend
 *
 * ❗ НЕ делает:
 * - localStorage
 * - AuthContext
 * - navigation
 *
 * 👉 Это чистый API слой (service layer)
 */
export async function login(
    data: LoginRequest
): Promise<AuthResponse> {

    /**
     * 🌐 POST /auth/login
     * отправляем username + password
     *
     * backend возвращает:
     * {
     *   accessToken,
     *   refreshToken
     * }
     */
    const response =
        await api.post(
            "/auth/login",
            data
        );

    /**
     * 📤 возвращаем только полезные данные
     * (axios оборачивает ответ в response.data)
     */
    return response.data;
}

/**
 * 📝 REGISTER REQUEST
 *
 * Создание нового пользователя
 *
 * ❗ Тоже чистый API слой:
 * не управляет состоянием приложения
 */
export async function register(
    data: RegisterRequest
): Promise<AuthResponse> {

    /**
     * 🌐 POST /auth/register
     * создаёт пользователя на backend
     */
    const response =
        await api.post(
            "/auth/register",
            data
        );

    /**
     * 📤 возвращаем результат backend-а
     */
    return response.data;
}