package com.jeannimi.messenger.application.notification.dto;

import java.time.Instant;

public record UserNotificationSettingsResult(
    boolean webPushEnabled, Instant createdAt, Instant updatedAt) {}
