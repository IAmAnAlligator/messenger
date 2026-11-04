package com.jeannimi.messenger.application.notification.dto;

import java.time.Instant;

public record UserNotificationSettingsResult(
    boolean webPushPrivateChatsEnabled,
    boolean webPushGroupChatsEnabled,
    boolean emailPrivateChatsEnabled,
    boolean emailGroupChatsEnabled,
    Instant createdAt,
    Instant updatedAt) {}
