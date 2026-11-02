package com.jeannimi.messenger.adapter.in.web.notification.dto;

import java.time.Instant;

public record UserNotificationSettingsResponse(
    boolean webPushPrivateChatsEnabled,
    boolean webPushGroupChatsEnabled,
    boolean emailPrivateChatsEnabled,
    boolean emailGroupChatsEnabled,
    Instant createdAt,
    Instant updatedAt) {}