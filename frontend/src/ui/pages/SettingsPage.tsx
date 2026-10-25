import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import { useAuth } from "../../contexts/AuthContext";

import {
    getWebPushSettings,
    subscribeToWebPush,
    unsubscribeFromWebPush,
} from "../../notifications/webPush";

import "../styles/settingsPage.css";

export default function NotificationSettingsPage() {
    const navigate = useNavigate();
    const { user } = useAuth();

    const [enabled, setEnabled] = useState(false);
    const [loading, setLoading] = useState(true);
    const [updating, setUpdating] = useState(false);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        let cancelled = false;

        async function loadSettings() {
            if (!user?.id) {
                setEnabled(false);
                setLoading(false);
                return;
            }

            setLoading(true);
            setError(null);

            try {
                const settings = await getWebPushSettings();

                if (!cancelled) {
                    setEnabled(settings.webPushEnabled);
                }
            } catch (exception) {
                if (!cancelled) {
                    setError(
                        exception instanceof Error
                            ? exception.message
                            : "Failed to load notification settings."
                    );
                }
            } finally {
                if (!cancelled) {
                    setLoading(false);
                }
            }
        }

        void loadSettings();

        return () => {
            cancelled = true;
        };
    }, [user?.id]);

    async function handleToggle() {
        if (updating || loading) {
            return;
        }

        setUpdating(true);
        setError(null);

        try {
            if (enabled) {
                await unsubscribeFromWebPush();
                setEnabled(false);
            } else {
                await subscribeToWebPush();
                setEnabled(true);
            }
        } catch (exception) {
            setError(
                exception instanceof Error
                    ? exception.message
                    : "Failed to update notification settings."
            );
        } finally {
            setUpdating(false);
        }
    }

    if (!user) {
        return (
            <main className="notification-settings-page">
                <p>Please sign in to manage your notification settings.</p>

                <button
                    type="button"
                    onClick={() => navigate("/")}
                >
                    Go to Home
                </button>
            </main>
        );
    }

    return (
        <main className="notification-settings-page">
            <header className="notification-settings-header">
                <button
                    type="button"
                    className="notification-back-button"
                    onClick={() => navigate("/chats")}
                >
                    ← Back
                </button>

                <h1>Notification Settings</h1>
            </header>

            <section className="notification-settings-card">
                <div className="notification-settings-description">
                    <h2>Message Notifications</h2>

                    <p>
                        Allow your browser to show notifications about new
                        messages, even when the messenger is closed.
                    </p>
                </div>

                <div className="notification-toggle-row">
                    <span className="notification-toggle-label">
                        {loading
                            ? "Loading settings..."
                            : enabled
                                ? "Notifications are enabled"
                                : "Notifications are disabled"}
                    </span>

                    <button
                        type="button"
                        role="switch"
                        aria-checked={enabled}
                        aria-label="Message notifications"
                        className={`notification-toggle ${enabled ? "is-enabled" : ""}`}
                        onClick={handleToggle}
                        disabled={loading || updating}
                    >
                        <span className="notification-toggle-thumb" />

                        <span className="notification-toggle-text">
                            {updating
                                ? "..."
                                : enabled
                                    ? "ON"
                                    : "OFF"}
                        </span>
                    </button>
                </div>

                {error && (
                    <p
                        className="notification-settings-error"
                        role="alert"
                    >
                        {error}
                    </p>
                )}

                {!loading &&
                    "Notification" in window &&
                    Notification.permission === "denied" && (
                        <p className="notification-settings-hint">
                            Notifications are blocked by your browser.
                            Allow notifications for this website in your
                            browser settings to receive Web Push messages.
                        </p>
                    )}
            </section>
        </main>
    );
}