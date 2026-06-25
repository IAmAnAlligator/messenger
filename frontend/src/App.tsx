import {
    BrowserRouter,
    Routes,
    Route
} from "react-router-dom";

import AuthPage from "./pages/AuthPage";

import ChatsPage from "./pages/ChatsPage";

import ChatPage from "./pages/ChatPage";

import ProtectedRoute from "./routes/ProtectedRoute";

/**
 * Главный компонент приложения
 */
function App() {

    return (
        <BrowserRouter>

            <Routes>

                {/* Публичная страница */}
                <Route
                    path="/"
                    element={<AuthPage />}
                />

                {/* Защищённые страницы */}
                <Route
                    path="/chats"
                    element={
                        <ProtectedRoute>
                            <ChatsPage />
                        </ProtectedRoute>
                    }
                />

                <Route
                    path="/chats/:chatId"
                    element={
                        <ProtectedRoute>
                            <ChatPage />
                        </ProtectedRoute>
                    }
                />

            </Routes>

        </BrowserRouter>
    );
}

export default App;