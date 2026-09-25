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
    AuthResponse,
} from "../api/auth";

/**
 * 🔐 LOGIN REQUEST
 *
 * Отвечает только за HTTP-запрос на backend.
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
     *
     * Отправляем:
     * {
     *   email,
     *   password
     * }
     */
    const response = await api.post(
        "/auth/login",
        data
    );

    /**
     * 📤 Возвращаем данные авторизации.
     */
    return response.data;
}

/**
 * 📝 REGISTER REQUEST
 *
 * Создание нового пользователя.
 *
 * Отвечает только за HTTP-запрос.
 */
export async function register(
    data: RegisterRequest
): Promise<AuthResponse> {

    /**
     * 🌐 POST /auth/register
     *
     * Отправляем:
     * {
     *   username,
     *   email,
     *   password
     * }
     */
    const response = await api.post(
        "/auth/register",
        data
    );

    /**
     * 📤 Возвращаем данные авторизации.
     */
    return response.data;
}