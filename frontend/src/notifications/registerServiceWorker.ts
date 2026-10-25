export async function registerServiceWorker(): Promise<ServiceWorkerRegistration> {
    if (!("serviceWorker" in navigator)) {
        throw new Error("Браузер не поддерживает Service Worker");
    }

    await navigator.serviceWorker.register("/sw.js");

    const registration = await navigator.serviceWorker.ready;

    if (!registration.active) {
        throw new Error("Service Worker не активирован");
    }

    return registration;
}