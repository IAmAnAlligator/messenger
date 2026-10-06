import {
    BrowserRouter,
    Routes,
    Route
} from "react-router-dom";

import AuthPage
    from "./ui/pages/AuthPage";

import ChatsPage
    from "./ui/pages/ChatsPage";

import ChatPage
    from "./ui/pages/ChatPage";

import ChatEditPage
    from "./ui/pages/ChatEditPage";

import ChatCreatePage
    from "./ui/pages/ChatCreatePage";

import ProfilePage
    from "./ui/pages/ProfilePage";

import ProtectedRoute
    from "./components/ProtectedRoute";

import MemberProfilePage
    from "./ui/pages/MemberProfilePage";

import GroupChatCreatePage
    from "./ui/pages/GroupChatCreatePage";

import PrivateChatCreatePage
    from "./ui/pages/PrivateChatCreatePage";

import NewPrivateChatPage
    from "./ui/pages/NewPrivateChatPage";


function App() {

    return (

        <div className="app">

            <BrowserRouter>

                <Routes>

                    <Route
                        path="/"
                        element={
                            <AuthPage />
                        }
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
                        path="/profile"
                        element={
                            <ProtectedRoute>
                                <ProfilePage />
                            </ProtectedRoute>
                        }
                    />


                    <Route
                        path="/chats/create/group"
                        element={
                            <ProtectedRoute>
                                <GroupChatCreatePage />
                            </ProtectedRoute>
                        }
                    />


                    <Route
                        path="/chats/create/private"
                        element={
                            <ProtectedRoute>
                                <PrivateChatCreatePage />
                            </ProtectedRoute>
                        }
                    />


                    <Route
                        path="/chats/new"
                        element={
                            <ProtectedRoute>
                                <NewPrivateChatPage />
                            </ProtectedRoute>
                        }
                    />


                    <Route
                        path="/chats/:chatId/members/:userId/profile"
                        element={
                            <ProtectedRoute>
                                <MemberProfilePage />
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
