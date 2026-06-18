import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import { api } from "../api/client";
import { useAuth } from "../auth/AuthContext";

/**
 * 💬 DTO чата, который приходит с backend
 */
type ChatDto = {
    id: number;
    name: string;
};

/**
 * 💬 Страница списка чатов
 *
 * RESPONSIBILITY:
 * - загрузить список чатов
 * - показать loading / empty states
 * - дать возможность выйти (logout)
 *
 * ❌ НЕ должна:
 * - управлять auth напрямую
 * - хранить токены
 */
export default function ChatsPage() {

    /**
     * 📦 список чатов
     */
    const [chats, setChats] =
        useState<ChatDto[]>([]);

    /**
     * ⏳ состояние загрузки списка чатов
     */
    const [loading, setLoading] =
        useState(true);

    /**
     * 🔐 logout берём из AuthContext
     * (единая точка управления авторизацией)
     */
    const { logout } = useAuth();

    /**
     * 🚀 навигация между страницами
     */
    const navigate =
        useNavigate();

    // =========================
    // 📡 LOAD CHATS
    // =========================
    useEffect(() => {
        loadChats();
    }, []);

    /**
     * 🌐 запрос списка чатов с backend
     *
     * ⚠️ важно:
     * api автоматически добавляет accessToken через interceptor
     */
    async function loadChats() {
        try {
            setLoading(true);

            /**
             * 📡 GET /chats
             * backend проверяет accessToken
             */
            const response =
                await api.get("/chats");

            /**
             * 💾 сохраняем данные в state
             */
            setChats(response.data);

        } catch (e) {
            /**
             * ❌ ошибка загрузки чатов
             * чаще всего:
             * - 401 (не авторизован)
             * - network error
             */
            console.error(
                "Failed to load chats",
                e
            );

        } finally {
            /**
             * ⏳ загрузка завершена в любом случае
             */
            setLoading(false);
        }
    }

    // =========================
    // 🚪 LOGOUT HANDLER
    // =========================
    async function handleLogout() {

        /**
         * 🔐 вызываем logout из AuthContext
         *
         * внутри:
         * - удаляется accessToken
         * - очищается user
         * - сбрасывается auth state
         */
        await logout();

        /**
         * 🚀 после logout перенаправляем на login
         *
         * replace = нельзя вернуться кнопкой Back
         */
        navigate("/", {
            replace: true,
        });
    }

    // =========================
    // 🎨 UI
    // =========================
    return (
        <div
            style={{
                padding: 20,
            }}
        >
            {/* =========================
                HEADER
            ========================= */}
            <div
                style={{
                    display: "flex",
                    justifyContent:
                        "space-between",
                    alignItems:
                        "center",
                    marginBottom: 20,
                }}
            >
                {/* 📌 заголовок страницы */}
                <h2>
                    Your chats
                </h2>

                {/* 🚪 logout кнопка */}
                <button
                    onClick={
                        handleLogout
                    }
                >
                    Logout
                </button>
            </div>

            {/* =========================
                LOADING STATE
            ========================= */}
            {loading && (
                <p>
                    Loading...
                </p>
            )}

            {/* =========================
                EMPTY STATE
            ========================= */}
            {!loading &&
                chats.length === 0 && (
                    <p>
                        No chats yet
                    </p>
                )}

            {/* =========================
                CHATS LIST
            ========================= */}
            <div>
                {chats.map((chat) => (
                    <div
                        key={chat.id}
                        style={{
                            padding: 10,
                            margin: 5,
                            border:
                                "1px solid #ccc",
                            borderRadius: 6,
                        }}
                    >
                        {/* 💬 имя чата */}
                        {chat.name}
                    </div>
                ))}
            </div>
        </div>
    );
}