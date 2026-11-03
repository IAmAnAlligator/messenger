import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";

import { useAuth } from "../../contexts/AuthContext";

import {
    getNotificationSettings,
    updateNotificationSettings,
    subscribeToWebPush,
    unsubscribeFromWebPush,
    type NotificationSettings,
} from "../../notifications/webPush";

import "../styles/settingsPage.css";

type SettingKey = keyof Pick<
    NotificationSettings,
    | "webPushPrivateChatsEnabled"
    | "webPushGroupChatsEnabled"
    | "emailPrivateChatsEnabled"
    | "emailGroupChatsEnabled"
>;

const initialSettings: NotificationSettings = {
    webPushPrivateChatsEnabled: false,
    webPushGroupChatsEnabled: false,
    emailPrivateChatsEnabled: false,
    emailGroupChatsEnabled: false,
    createdAt: "",
    updatedAt: "",
};

export default function NotificationSettingsPage() {
    const navigate = useNavigate();
    const { user } = useAuth();

    const [settings, setSettings] =
        useState<NotificationSettings>(initialSettings);

    const [loading, setLoading] = useState(true);
    const [updating, setUpdating] = useState<SettingKey | null>(null);
    const [error, setError] = useState<string | null>(null);

    useEffect(() => {
        let cancelled = false;

        async function loadSettings() {
            if (!user?.id) {
                setSettings(initialSettings);
                setLoading(false);
                return;
            }

            setLoading(true);
            setError(null);

            try {
                const result = await getNotificationSettings();

                if (!cancelled) {
                    setSettings(result);
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

    async function handleToggle(key: SettingKey) {
        if (updating !== null || loading) {
            return;
        }

        const newValue = !settings[key];

        const isWebPushSetting =
            key === "webPushPrivateChatsEnabled" ||
            key === "webPushGroupChatsEnabled";

        const otherWebPushEnabled =
            key === "webPushPrivateChatsEnabled"
                ? settings.webPushGroupChatsEnabled
                : settings.webPushPrivateChatsEnabled;

        setUpdating(key);
        setError(null);

        try {
            if (isWebPushSetting && newValue) {
                await subscribeToWebPush();
            }

            const updatedSettings = await updateNotificationSettings({
                [key]: newValue,
            });

            setSettings(updatedSettings);

            if (isWebPushSetting && !newValue && !otherWebPushEnabled) {
                await unsubscribeFromWebPush();
            }
        } catch (exception) {
            setError(
                exception instanceof Error
                    ? exception.message
                    : "Failed to update notification settings."
            );
        } finally {
            setUpdating(null);
        }
    }

    function renderToggle(
        key: SettingKey,
        label: string,
        description: string
    ) {
        const enabled = settings[key];

        return (
            <div className="notification-toggle-row" key={key}>
                <div className="notification-toggle-info">
                    <span className="notification-toggle-label">
                        {label}
                    </span>

                    <p>{description}</p>
                </div>

                <button
                    type="button"
                    role="switch"
                    aria-checked={enabled}
                    aria-label={label}
                    className={`notification-toggle ${
                        enabled ? "is-enabled" : ""
                    }`}
                    onClick={() => void handleToggle(key)}
                    disabled={loading || updating !== null}
                >
                    <span className="notification-toggle-thumb" />

                    <span className="notification-toggle-text">
                        {updating === key
                            ? "..."
                            : enabled
                                ? "ON"
                                : "OFF"}
                    </span>
                </button>
            </div>
        );
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

            {loading && (
                <p>Loading notification settings...</p>
            )}

            <section className="notification-settings-card">
                <div className="notification-settings-description">
                    <h2>Web Push Notifications</h2>

                    <p>
                        Allow your browser to show notifications about new
                        messages, even when the messenger is closed.
                    </p>
                </div>

                {renderToggle(
                    "webPushPrivateChatsEnabled",
                    "Private Chats",
                    "Receive browser notifications about messages in private chats."
                )}

                {renderToggle(
                    "webPushGroupChatsEnabled",
                    "Group Chats",
                    "Receive browser notifications about messages in group chats."
                )}
            </section>

            <section className="notification-settings-card">
                <div className="notification-settings-description">
                    <h2>Email Notifications</h2>

                    <p>
                        Receive email notifications about new messages.
                    </p>
                </div>

                {renderToggle(
                    "emailPrivateChatsEnabled",
                    "Private Chats",
                    "Receive email notifications about messages in private chats."
                )}

                {renderToggle(
                    "emailGroupChatsEnabled",
                    "Group Chats",
                    "Receive email notifications about messages in group chats."
                )}
            </section>

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
        </main>
    );
}