package com.jeannimi.messenger.application.outbox;

import com.jeannimi.messenger.domain.event.EventId;
import com.jeannimi.messenger.domain.outbox.AggregateId;
import com.jeannimi.messenger.domain.outbox.OutboxEventId;
import java.time.Instant;

public record OutboxEventData(
    OutboxEventId id,
    EventId eventId,
    String topic,
    String eventType,
    AggregateId aggregateId,
    String payload,
    int attemptCount,
    Instant nextAttemptAt) {}