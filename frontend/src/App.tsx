import {
    BrowserRouter,
    Routes,
    Route
} from "react-router-dom";

// 📄 Страница авторизации (логин/регистрация)
import AuthPage from "./pages/AuthPage";

// 💬 Страница чатов (доступ только после авторизации)
import ChatsPage from "./pages/ChatsPage";

// 🔐 Компонент-защита маршрутов (проверяет user из AuthContext)
import ProtectedRoute from "./routes/ProtectedRoute";

/**
 * Главный компонент приложения
 * Здесь настраиваются все маршруты (routes)
 */
function App() {
    return (
        // 🌐 BrowserRouter включает поддержку маршрутов в браузере
        <BrowserRouter>

            {/* 📍 Все маршруты приложения */}
            <Routes>

                {/* =========================
                    🟢 ПУБЛИЧНЫЙ РОУТ
                    ========================= */}

                {/* 
                    "/" — страница входа
                    Доступна ВСЕМ пользователям (даже неавторизованным)
                */}
                <Route
                    path="/"
                    element={<AuthPage />}
                />

                {/* =========================
                    🔐 ЗАЩИЩЁННЫЙ РОУТ
                    ========================= */}

                {/* 
                    "/chats" — приватная страница
                    Доступна только если:
                    - user существует в AuthContext
                    - loading = false
                    Иначе произойдёт redirect на "/"
                */}
                <Route
                    path="/chats"
                    element={
                        <ProtectedRoute>
                            {/* 
                                ChatsPage НЕ рендерится напрямую.
                                Сначала ProtectedRoute проверяет авторизацию.
                                Если user есть → показываем ChatsPage
                                Если нет → редирект на "/"
                            */}
                            <ChatsPage />
                        </ProtectedRoute>
                    }
                />

            </Routes>
        </BrowserRouter>
    );
}

export default App;