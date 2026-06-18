import {
    createContext,
    useContext,
    useEffect,
    useRef,
    useState,
} from "react";

import { api } from "../api/client";

/**
 * 👤 Модель пользователя в приложении
 * ВАЖНО: уже "очищенная" версия DTO с backend
 * (без nested username.value)
 */
type User = {
    id: number;
    username: string;
};

/**
 * 📦 Что доступно во всём приложении через useAuth()
 */
type AuthContextType = {
    user: User | null;              // текущий пользователь (null = не авторизован)
    loading: boolean;              // идёт ли проверка авторизации
    login: (access: string) => Promise<void>;  // вход
    logout: () => Promise<void>;              // выход
};

/**
 * 🧠 React Context для авторизации
 * null = используется только внутри Provider
 */
const AuthContext =
    createContext<AuthContextType | null>(null);

/**
 * 🧩 Provider, который оборачивает всё приложение
 * Именно он управляет состоянием авторизации
 */
export function AuthProvider({
    children,
}: {
    children: React.ReactNode;
}) {
    /**
     * 👤 текущий пользователь
     */
    const [user, setUser] =
        useState<User | null>(null);

    /**
     * ⏳ глобальный флаг загрузки авторизации
     * true = приложение ещё проверяет /users/me
     */
    const [loading, setLoading] =
        useState(true);

    /**
     * 🔥 защита от повторного вызова fetchMe()
     *
     * Почему это нужно:
     * - React StrictMode может вызвать useEffect 2 раза
     * - logout + rerender могут повторно триггерить init
     * - иначе можно получить лишние запросы /users/me
     */
    const initialized = useRef(false);

    // =========================
    // 📡 FETCH CURRENT USER
    // =========================
    async function fetchMe() {
        try {
            /**
             * 🔐 проверяем, есть ли accessToken
             * если нет — нет смысла дергать backend
             */
            const token =
                localStorage.getItem("accessToken");

            if (!token) {
                setUser(null);
                return;
            }

            /**
             * 🌐 запрос текущего пользователя
             * backend должен проверить accessToken
             */
            const res =
                await api.get("/users/me");

            /**
             * 🧹 нормализуем DTO → frontend модель
             */
            setUser({
                id: res.data.id,
                username:
                    res.data.username.value,
            });

        } catch (e) {
            /**
             * ❌ если token невалидный / 401
             * → очищаем состояние
             */
            setUser(null);

            localStorage.removeItem("accessToken");
        } finally {
            /**
             * ⏳ в любом случае загрузка завершена
             */
            setLoading(false);
        }
    }

    // =========================
    // 🚀 INITIAL AUTH CHECK
    // =========================
    useEffect(() => {
        /**
         * 🔥 защита от двойного запуска (StrictMode + rerender)
         */
        if (initialized.current) return;

        initialized.current = true;

        /**
         * 🔍 при старте приложения проверяем сессию
         */
        fetchMe();
    }, []);

    // =========================
    // 🔑 LOGIN
    // =========================
    async function login(access: string) {
        /**
         * 💾 сохраняем access token
         * (используется axios interceptor)
         */
        localStorage.setItem("accessToken", access);

        /**
         * ⏳ показываем загрузку пока обновляем user
         */
        setLoading(true);

        /**
         * 🔄 подтягиваем пользователя с backend
         */
        await fetchMe();
    }

    // =========================
    // 🚪 LOGOUT
    // =========================
    async function logout() {
        /**
         * 🔥 СНАЧАЛА локально разлогиниваем
         * чтобы:
         * - ProtectedRoute сразу заблокировал доступ
         * - axios interceptor не пытался "восстановить" сессию
         */
        localStorage.removeItem("accessToken");

        setUser(null);
        setLoading(false);

        try {
            /**
             * 🌐 уведомляем backend о logout
             * (например, инвалидировать refresh token)
             */
            await api.post("/auth/logout");
        } catch {
            /**
             * ❗ ignore errors
             * logout должен быть "best effort"
             */
        }
    }

    // =========================
    // 📦 PROVIDER VALUE
    // =========================
    return (
        <AuthContext.Provider
            value={{
                user,
                loading,
                login,
                logout,
            }}
        >
            {children}
        </AuthContext.Provider>
    );
}

/**
 * 🪝 Хук для доступа к auth state
 */
export function useAuth() {
    const ctx =
        useContext(AuthContext);

    /**
     * ❗ защита от использования вне Provider
     */
    if (!ctx) {
        throw new Error(
            "useAuth must be used inside AuthProvider"
        );
    }

    return ctx;
}