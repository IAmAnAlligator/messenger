import { api } from "../api/client";
import { registerServiceWorker } from "./registerServiceWorker";

interface WebPushSubscriptionRequest {
    endpoint: string;
    p256dh: string;
    auth: string;
}

export interface WebPushSettings {
    webPushEnabled: boolean;
}

function urlBase64ToUint8Array(base64String: string): Uint8Array {
    const padding = "=".repeat((4 - (base64String.length % 4)) % 4);

    const base64 = (base64String + padding)
        .replace(/-/g, "+")
        .replace(/_/g, "/");

    const rawData = window.atob(base64);

    return Uint8Array.from(rawData, (character) =>
        character.charCodeAt(0)
    );
}

function getSubscriptionRequest(
    subscription: PushSubscription
): WebPushSubscriptionRequest {
    const json = subscription.toJSON();

    const p256dh = json.keys?.p256dh;
    const auth = json.keys?.auth;

    if (!p256dh || !auth) {
        throw new Error("The browser did not provide Web Push encryption keys.");
    }

    return {
        endpoint: subscription.endpoint,
        p256dh,
        auth,
    };
}

export async function getWebPushSettings(): Promise<WebPushSettings> {
    const response = await api.get<WebPushSettings>(
        "/notifications/settings"
    );

    return response.data;
}

export async function subscribeToWebPush(): Promise<void> {
    if (!("serviceWorker" in navigator) || !("PushManager" in window)) {
        throw new Error("Your browser does not support Web Push.");
    }

    if (!("Notification" in window)) {
        throw new Error("Your browser does not support notifications.");
    }

    const publicKey = import.meta.env.VITE_WEB_PUSH_VAPID_PUBLIC_KEY;

    if (!publicKey) {
        throw new Error("The public VAPID key is not configured.");
    }

    const permission = await Notification.requestPermission();

    if (permission !== "granted") {
        throw new Error("Notification permission was not granted.");
    }

    const registration = await registerServiceWorker();

    let subscription = await registration.pushManager.getSubscription();

    if (!subscription) {
        subscription = await registration.pushManager.subscribe({
            userVisibleOnly: true,
            applicationServerKey: urlBase64ToUint8Array(publicKey).buffer as ArrayBuffer,
        });
    }

    // Save the browser subscription for the authenticated user.
    await api.post(
        "/notifications/push/subscriptions",
        getSubscriptionRequest(subscription)
    );

    // Enable notifications for the current user.
    await api.patch("/notifications/settings", {
        webPushEnabled: true,
    });
}

export async function unsubscribeFromWebPush(): Promise<void> {
    if (!("serviceWorker" in navigator)) {
        throw new Error("Your browser does not support Service Workers.");
    }

    const registration = await navigator.serviceWorker.getRegistration("/");

    if (!registration) {
        await api.patch("/notifications/settings", {
            webPushEnabled: false,
        });
        return;
    }

    const subscription = await registration.pushManager.getSubscription();

    if (!subscription) {
        await api.patch("/notifications/settings", {
            webPushEnabled: false,
        });
        return;
    }

    // Disable notifications for the current user first.
    await api.patch("/notifications/settings", {
        webPushEnabled: false,
    });

    await api.delete("/notifications/push/subscriptions", {
        params: {
            endpoint: subscription.endpoint,
        },
    });

    // Keep the browser subscription so it can be reused by another account.
    // await subscription.unsubscribe();
}

export async function syncWebPushSubscription(): Promise<void> {
    if (!("serviceWorker" in navigator) || !("PushManager" in window)) {
        return;
    }

    if (!("Notification" in window)) {
        return;
    }

    if (Notification.permission !== "granted") {
        return;
    }

    const settings = await getWebPushSettings();

    if (!settings.webPushEnabled) {
        return;
    }

    const registration = await registerServiceWorker();

    const subscription = await registration.pushManager.getSubscription();

    if (!subscription) {
        return;
    }

    await api.post(
        "/notifications/push/subscriptions",
        getSubscriptionRequest(subscription)
    );
}