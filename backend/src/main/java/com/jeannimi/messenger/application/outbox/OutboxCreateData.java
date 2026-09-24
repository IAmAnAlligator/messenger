package com.jeannimi.messenger.application.outbox;

import java.util.UUID;

public record OutboxCreateData(
    UUID eventId,
    String topic,
    String eventType,
    String aggregateId,
    String payload) {}