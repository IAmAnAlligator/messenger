package com.jeannimi.messenger.application.outbox;

import java.util.UUID;

public record OutboxEventData(
    Long id,
    UUID eventId,
    String topic,
    String eventType,
    String aggregateId,
    String payload) {}