import { useState } from "react";
import {
    subscribeToWebPush,
    unsubscribeFromWebPush,
} from "../notifications/webPush";

interface EnableWebPushButtonProps {
    enabled: boolean;
    onEnabledChange: (enabled: boolean) => void;
}

export default function EnableWebPushButton({
    enabled,
    onEnabledChange,
}: EnableWebPushButtonProps) {
    const [loading, setLoading] = useState(false);
    const [error, setError] = useState<string | null>(null);

    async function handleClick() {
        setLoading(true);
        setError(null);

        try {
            if (enabled) {
                await unsubscribeFromWebPush();
                onEnabledChange(false);
            } else {
                await subscribeToWebPush();
                onEnabledChange(true);
            }
        } catch (exception) {
            setError(
                exception instanceof Error
                    ? exception.message
                    : "Не удалось изменить настройки уведомлений"
            );
        } finally {
            setLoading(false);
        }
    }

    return (
        <div>
            <button
                type="button"
                onClick={handleClick}
                disabled={loading}
            >
                {loading
                    ? "Обработка..."
                    : enabled
                        ? "Отключить уведомления"
                        : "Включить уведомления"}
            </button>

            {error && <p role="alert">{error}</p>}
        </div>
    );
}