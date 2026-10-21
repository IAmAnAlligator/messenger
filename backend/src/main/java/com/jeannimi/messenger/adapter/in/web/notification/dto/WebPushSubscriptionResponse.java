package com.jeannimi.messenger.adapter.in.web.notification.dto;

import java.time.Instant;
import java.util.UUID;

public record WebPushSubscriptionResponse(
    UUID id, String endpoint, Instant createdAt, Instant lastUsedAt) {}
