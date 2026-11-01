package com.jeannimi.messenger.adapter.in.web.notification.dto;

import java.time.Instant;

public record UserNotificationSettingsResponse(
    boolean webPushEnabled,
    boolean emailEnabled,
    Instant createdAt,
    Instant updatedAt) {}
