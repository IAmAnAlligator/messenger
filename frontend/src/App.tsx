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

import ProtectedRoute from "./components/ProtectedRoute";

/**
 * Главный компонент приложения
 */
function App() {

    return (

        <div className="app">

            <BrowserRouter>

                <Routes>


                    <Route
                        path="/"
                        element={<AuthPage />}
                    />


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
                        element={
                            <ProtectedRoute>
                                <ChatCreatePage />
                            </ProtectedRoute>
                        }
                    />


                    <Route
                        path="/chats/:chatId/edit"
                        element={
                            <ProtectedRoute>
                                <ChatEditPage />
                            </ProtectedRoute>
                        }
                    />


                </Routes>

            </BrowserRouter>

        </div>

    );

}

export default App;
