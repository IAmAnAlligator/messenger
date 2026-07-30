import {
    BrowserRouter,
    Routes,
    Route
} from "react-router-dom";

import AuthPage from "./ui/pages/AuthPage";

import ChatsPage from "./ui/pages/ChatsPage";

import ChatPage from "./ui/pages/ChatPage";

import ChatEditPage from "./ui/pages/ChatEditPage";

import ChatCreatePage from "./ui/pages/ChatCreatePage";

import ProtectedRoute from "./auth/ProtectedRoute";

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

                <Route
    path="/chats/create"
    element={<ChatCreatePage />}
/>

                <Route
    path="/chats/:chatId/edit"
    element={
        <ChatEditPage />
    }
/>

            </Routes>

        </BrowserRouter>
    );
}

export default App;