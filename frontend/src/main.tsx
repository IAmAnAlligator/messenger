import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App";
import { AuthProvider } from "./contexts/AuthContext";
import {
    ChatsProvider
} from "./contexts/ChatsContext";

ReactDOM.createRoot(document.getElementById("root")!).render(
    <React.StrictMode>

        <AuthProvider>

            <ChatsProvider>

                <App />

            </ChatsProvider>

        </AuthProvider>

    </React.StrictMode>
);