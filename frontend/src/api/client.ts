import axios from "axios";
import { refreshToken } from "./refresh";

// Создание клиента
export const api = axios.create({
    baseURL: "http://localhost:8080/api"
});

// =======================
// REQUEST INTERCEPTOR
// =======================
// Подстановка access token, выполняется перед каждым запросом
api.interceptors.request.use((config) => {

    const token = localStorage.getItem("accessToken");

    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
});

// =======================
// REFRESH LOGIC STATE
// =======================
// Глобальное состояние refresh, очередь, чтобы не было путаницы
let isRefreshing = false;
let queue: any[] = [];

// =======================
// RESPONSE INTERCEPTOR
// =======================
api.interceptors.response.use(
    (res) => res,
    async (error) => {

        const originalRequest = error.config;

        if (error.response?.status === 401 && !originalRequest._retry) {

            console.log("401 intercepted");

            originalRequest._retry = true;

            if (isRefreshing) {
                return new Promise((resolve) => {
                    queue.push(() => resolve(api(originalRequest)));
                });
            }

            isRefreshing = true;

            try {
                const data = await refreshToken();

                localStorage.setItem("accessToken", data.accessToken);
                localStorage.setItem("refreshToken", data.refreshToken);

                isRefreshing = false;

                queue.forEach((cb) => cb());
                queue = [];

                return api(originalRequest);

            } catch (e) {

                localStorage.removeItem("accessToken");
                localStorage.removeItem("refreshToken");

                window.location.href = "/";

                return Promise.reject(e);
            }
        }

        return Promise.reject(error);
    }
);