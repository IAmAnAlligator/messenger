package com.jeannimi.messenger.adapter.out.persistence;

import com.jeannimi.messenger.application.outbox.OutboxCreateData;
import com.jeannimi.messenger.application.outbox.OutboxEventData;
import com.jeannimi.messenger.domain.outbox.OutboxEvent;

public final class OutboxEventMapper {

  private OutboxEventMapper() {}

  public static OutboxEvent toDomain(OutboxCreateData data) {
    return OutboxEvent.create(
        data.eventId(),
        data.topic(),
        data.eventType(),
        data.aggregateId(),
        data.payload());
  }

  public static OutboxEventData toData(OutboxEvent event) {
    return new OutboxEventData(
        event.getId(),
        event.getEventId(),
        event.getTopic(),
        event.getEventType(),
        event.getAggregateId(),
        event.getPayload(),
        event.getAttemptCount(),
        event.getNextAttemptAt());
  }
}