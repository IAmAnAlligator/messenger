import { Navigate } from "react-router-dom";
import type { ReactElement } from "react";

import { useAuth } from "../auth/AuthContext";

/**
 * 🔐 ProtectedRoute — компонент-защитник маршрутов
 *
 * Его задача:
 * - проверить авторизацию пользователя
 * - решить, можно ли показать страницу
 * - либо перенаправить на login
 *
 * ❗ ВАЖНО:
 * Он НЕ должен делать API запросы или side effects
 * Он только принимает решение на основе AuthContext
 */
export default function ProtectedRoute({
    children,
}: {
    children: ReactElement;
}) {

    /**
     * 👤 берём состояние авторизации из контекста
     *
     * user:
     *   - объект пользователя → значит авторизован
     *   - null → не авторизован
     *
     * loading:
     *   - true → ещё проверяем /users/me
     *   - false → проверка завершена
     */
    const { user, loading } = useAuth();

    /**
     * ⏳ пока идёт проверка авторизации
     *
     * Почему return null:
     * - не показываем ни страницу
     * - не делаем редирект раньше времени
     *
     * Это защищает от "мигания" редиректа на login
     */
    if (loading) return null;

    /**
     * 🔐 если пользователь есть → разрешаем доступ
     */
    if (user) {
        return children;
    }

    /**
     * 🚪 если user === null → редирект на страницу входа
     *
     * replace = заменяет историю,
     * чтобы нельзя было нажать "Back" и вернуться в protected page
     */
    return (
        <Navigate
            to="/"
            replace
        />
    );
}