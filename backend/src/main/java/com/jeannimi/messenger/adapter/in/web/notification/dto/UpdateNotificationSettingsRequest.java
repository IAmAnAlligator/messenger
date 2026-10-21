package com.jeannimi.messenger.adapter.in.web.notification.dto;

import jakarta.validation.constraints.NotNull;

public record UpdateNotificationSettingsRequest(@NotNull Boolean webPushEnabled) {}
