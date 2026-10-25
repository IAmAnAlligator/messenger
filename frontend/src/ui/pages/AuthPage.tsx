import { useState } from "react";
import { useNavigate } from "react-router-dom";

import {
    login as loginRequest,
    register,
} from "../../services/authService";

import { useAuth } from "../../contexts/AuthContext.tsx";
import AuthLayout from "../layouts/AuthLayout.tsx";
import AuthForm from "../components/auth/AuthForm.tsx";

import { subscribeToWebPush } from "../../notifications/webPush";

import "../styles/auth.css";

export default function AuthPage() {
    const [username, setUsername] = useState("");
    const [email, setEmail] = useState("");
    const [password, setPassword] = useState("");

    const [showNotificationPrompt, setShowNotificationPrompt] = useState(false);
    const [notificationError, setNotificationError] = useState<string | null>(null);
    const [enablingNotifications, setEnablingNotifications] = useState(false);

    const navigate = useNavigate();
    const { login } = useAuth();

    async function handleLogin() {
        try {
            const auth = await loginRequest({
                email,
                password,
            });

            await login(auth.accessToken);

            navigate("/chats", { replace: true });
        } catch {
            alert("Login failed. Please check your credentials and try again.");
        }
    }

    async function handleRegister() {
        try {
            const auth = await register({
                username,
                email,
                password,
            });

            await login(auth.accessToken);

            setShowNotificationPrompt(true);
        } catch {
            alert("Registration failed. Please try again.");
        }
    }

    async function handleEnableNotifications() {
        if (enablingNotifications) {
            return;
        }

        setEnablingNotifications(true);
        setNotificationError(null);

        try {
            await subscribeToWebPush();

            setShowNotificationPrompt(false);
            navigate("/chats", { replace: true });
        } catch (exception) {
            setNotificationError(
                exception instanceof Error
                    ? exception.message
                    : "Failed to enable notifications. Please try again."
            );
        } finally {
            setEnablingNotifications(false);
        }
    }

    function handleSkipNotifications() {
        setShowNotificationPrompt(false);
        navigate("/chats", { replace: true });
    }

    return (
        <AuthLayout>
            <AuthForm
                username={username}
                email={email}
                password={password}
                onUsernameChange={setUsername}
                onEmailChange={setEmail}
                onPasswordChange={setPassword}
                onLogin={handleLogin}
                onRegister={handleRegister}
            />

            {showNotificationPrompt && (
                <div className="notification-prompt-overlay">
                    <section
                        className="notification-prompt"
                        role="dialog"
                        aria-modal="true"
                        aria-labelledby="notification-prompt-title"
                    >
                        <h2 id="notification-prompt-title">
                            Enable Notifications
                        </h2>

                        <p>
                            Get notified when you receive new messages,
                            even when the messenger is closed.
                        </p>

                        {notificationError && (
                            <p
                                className="notification-prompt-error"
                                role="alert"
                            >
                                {notificationError}
                            </p>
                        )}

                        <div className="notification-prompt-actions">
                            <button
                                type="button"
                                onClick={handleEnableNotifications}
                                disabled={enablingNotifications}
                            >
                                {enablingNotifications
                                    ? "Enabling..."
                                    : "Enable notifications"}
                            </button>

                            <button
                                type="button"
                                onClick={handleSkipNotifications}
                                disabled={enablingNotifications}
                            >
                                Not now
                            </button>
                        </div>
                    </section>
                </div>
            )}
        </AuthLayout>
    );
}