import {
    BrowserRouter,
    Routes,
    Route
} from "react-router-dom";

import AuthPage from "./pages/AuthPage";

import ChatsPage from "./pages/ChatsPage";

import ChatPage from "./pages/ChatPage";

import ChatEditPage from "./pages/ChatEditPage";

import ChatCreatePage from "./pages/ChatCreatePage";

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