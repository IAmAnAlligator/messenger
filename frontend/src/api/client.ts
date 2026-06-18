import axios from "axios";
import { refreshToken } from "./refresh";

/**
 * 🔧 Базовый axios-инстанс для всего приложения
 * Все запросы идут через него
 */
export const api = axios.create({
    baseURL: "http://localhost:8080/api",

    /**
     * 🍪 withCredentials = true
     * позволяет браузеру отправлять cookies (refreshToken HttpOnly cookie)
     */
    withCredentials: true
});

// =======================
// REQUEST INTERCEPTOR
// =======================

/**
 * 🚀 Срабатывает ПЕРЕД каждым запросом
 * Используется для добавления Authorization header
 */
api.interceptors.request.use((config) => {

    // 📦 достаём accessToken из localStorage
    const token =
        localStorage.getItem("accessToken");

    /**
     * 🔐 если token есть — добавляем его в заголовок
     * формат:
     * Authorization: Bearer <token>
     */
    if (token) {
        config.headers.Authorization =
            `Bearer ${token}`;
    }

    return config;
});

// =======================
// REFRESH CONTROL STATE
// =======================

/**
 * 🔁 флаг: сейчас идёт refresh token запрос?
 * нужен чтобы НЕ запускать несколько refresh одновременно
 */
let isRefreshing = false;

/**
 * ⏳ очередь запросов, которые ждут refresh
 * если несколько запросов получили 401 одновременно
 */
let queue: Array<() => void> = [];

// =======================
// RESPONSE INTERCEPTOR
// =======================

/**
 * 🚨 Срабатывает на ответ сервера (success + error)
 */
api.interceptors.response.use(
    // ✅ если запрос успешный — просто возвращаем ответ
    (res) => res,

    // ❌ обработка ошибок
    async (error) => {

        /**
         * 📌 оригинальный запрос, который упал
         * (например /users/me)
         */
        const originalRequest =
            error.config;

        /**
         * 🔥 логика refresh token:
         * срабатывает только если:
         * - 401 Unauthorized
         * - запрос ещё НЕ ретраился (_retry)
         */
        if (
            error.response?.status === 401 &&
            !originalRequest._retry
        ) {

            /**
             * 🚫 защита от бесконечного цикла
             */
            originalRequest._retry = true;

            /**
             * ⏳ если refresh уже идёт —
             * кладём запрос в очередь
             */
            if (isRefreshing) {

                return new Promise((resolve) => {

                    queue.push(() => {
                        resolve(api(originalRequest));
                    });

                });
            }

            /**
             * 🔄 начинаем refresh процесс
             */
            isRefreshing = true;

            try {
                /**
                 * 🔑 запрос на обновление access token
                 * refreshToken берётся из HttpOnly cookie
                 */
                const data = await refreshToken();

                /**
                 * 💾 сохраняем новый access token
                 */
                localStorage.setItem(
                    "accessToken",
                    data.accessToken
                );

                /**
                 * ✅ refresh завершён
                 */
                isRefreshing = false;

                /**
                 * 🚀 выполняем все запросы из очереди
                 * (которые ждали refresh)
                 */
                queue.forEach((cb) => cb());

                queue = [];

                /**
                 * 🔁 повторяем оригинальный запрос
                 */
                return api(originalRequest);

            } catch (e) {

                /**
                 * ❌ refresh не удался:
                 * - refresh token истёк
                 * - сервер отклонил
                 */
                isRefreshing = false;

                queue = [];

                /**
                 * 🧹 чистим access token
                 */
                localStorage.removeItem("accessToken");

                /**
                 * 🚪 выкидываем пользователя на login
                 * (жёсткий logout)
                 */
                window.location.href = "/";

                return Promise.reject(e);
            }
        }

        /**
         * ❗ если ошибка не 401 — просто пробрасываем дальше
         */
        return Promise.reject(error);
    }
);