self.addEventListener("push", (event) => {
    if (!event.data) {
        return;
    }

    let payload;

    try {
        payload = event.data.json();
    } catch {
        payload = {
            title: "New message",
            body: event.data.text(),
        };
    }

    event.waitUntil(
        self.registration.showNotification(
            payload.title || "New message",
            {
                body: payload.body || "",
                data: payload.data || {},
                tag: payload.data?.messageId || undefined,
            }
        )
    );
});

self.addEventListener("notificationclick", (event) => {
    event.notification.close();

    const chatId = event.notification.data?.chatId;
    const targetUrl = chatId
        ? `/chats/${encodeURIComponent(chatId)}`
        : "/";

    event.waitUntil(
        clients.matchAll({
            type: "window",
            includeUncontrolled: true,
        }).then(async (windowClients) => {
            for (const client of windowClients) {
                if (new URL(client.url).origin !== self.location.origin) {
                    continue;
                }

                await client.navigate(targetUrl);
                await client.focus();
                return;
            }

            await clients.openWindow(targetUrl);
        })
    );
});