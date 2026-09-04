package com.jeannimi.messenger.adapter.out.persistence;

import com.jeannimi.messenger.application.outbox.OutboxEventData;
import com.jeannimi.messenger.outbox.entity.OutboxEvent;

public final class OutboxEventMapper {

  private OutboxEventMapper() {}

  public static OutboxEvent toEntity(OutboxEventData data) {
    return OutboxEvent.pending(
        data.eventId(),
        data.topic(),
        data.eventType(),
        data.aggregateId(),
        data.payload());
  }

  public static OutboxEventData toData(OutboxEvent entity) {
    return new OutboxEventData(
        entity.getId(),
        entity.getEventId(),
        entity.getTopic(),
        entity.getEventType(),
        entity.getAggregateId(),
        entity.getPayload());
  }
}