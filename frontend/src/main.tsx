import React from "react";
import ReactDOM from "react-dom/client";
import App from "./App";
import { AuthProvider } from "./contexts/AuthContext";
import { ChatsProvider } from "./contexts/ChatsContext";
import { registerServiceWorker } from "./notifications/registerServiceWorker";

registerServiceWorker().catch((error: unknown) => {
    console.error("Не удалось зарегистрировать Service Worker", error);
});

ReactDOM.createRoot(document.getElementById("root")!).render(
    <React.StrictMode>
        <AuthProvider>
            <ChatsProvider>
                <App />
            </ChatsProvider>
        </AuthProvider>
    </React.StrictMode>
);