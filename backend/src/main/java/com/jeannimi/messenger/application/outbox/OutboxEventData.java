package com.jeannimi.messenger.application.outbox;

import java.time.Instant;
import java.util.UUID;

public record OutboxEventData(
    Long id, UUID eventId, String topic, String eventType, String aggregateId, String payload
, int attemptCount, Instant nextAttemptAt) {}
