package com.jeannimi.messenger.adapter.in.web.notification.dto;

import jakarta.validation.constraints.NotBlank;

public record WebPushSubscriptionRequest(
    @NotBlank String endpoint, @NotBlank String p256dh, @NotBlank String auth) {}
