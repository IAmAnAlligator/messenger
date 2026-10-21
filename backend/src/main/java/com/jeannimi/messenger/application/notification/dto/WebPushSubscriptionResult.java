package com.jeannimi.messenger.application.notification.dto;

import java.time.Instant;
import java.util.UUID;

public record WebPushSubscriptionResult(
    UUID id, String endpoint, Instant createdAt, Instant lastUsedAt) {}
