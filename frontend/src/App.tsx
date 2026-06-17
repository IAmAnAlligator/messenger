import {
    BrowserRouter,
    Routes,
    Route
} from "react-router-dom";

import AuthPage from "./pages/AuthPage";
import ChatsPage from "./pages/ChatsPage";

import ProtectedRoute from "./routes/ProtectedRoute";

function App() {
    return (
        <BrowserRouter>
            <Routes>

                {/* публичная страница */}
                <Route
                    path="/"
                    element={<AuthPage />}
                />

                защищённая страница
                <Route
                    path="/chats"
                    element={
                        <ProtectedRoute>
                            <ChatsPage />
                        </ProtectedRoute>
                    }
                />

            </Routes>
        </BrowserRouter>
    );
}

export default App;