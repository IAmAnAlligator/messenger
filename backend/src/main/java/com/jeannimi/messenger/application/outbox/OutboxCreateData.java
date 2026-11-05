package com.jeannimi.messenger.application.outbox;

import com.jeannimi.messenger.domain.common.EventId;
import com.jeannimi.messenger.domain.outbox.AggregateId;
import com.jeannimi.messenger.domain.outbox.OutboxEventId;

public record OutboxCreateData(
    OutboxEventId id,
    EventId eventId,
    String topic,
    String eventType,
    AggregateId aggregateId,
    String payload) {}
