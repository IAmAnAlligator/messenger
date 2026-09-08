package com.jeannimi.messenger.adapter.out.persistence;

import com.jeannimi.messenger.application.outbox.OutboxEventData;
import com.jeannimi.messenger.domain.outbox.OutboxEvent;

public final class OutboxEventMapper {

  private OutboxEventMapper() {}

  public static OutboxEvent toEntity(OutboxEventData data) {
    return OutboxEvent.create(
        data.eventId(), data.topic(), data.eventType(), data.aggregateId(), data.payload());
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
