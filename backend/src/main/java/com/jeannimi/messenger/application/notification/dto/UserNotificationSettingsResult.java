package com.jeannimi.messenger.application.notification.dto;

import java.time.Instant;

public record UserNotificationSettingsResult(
    boolean webPushEnabled,
    boolean emailEnabled,
    Instant createdAt,
    Instant updatedAt) {}
