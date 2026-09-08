package com.jeannimi.messenger.adapter.out.persistence.mapper;

import com.jeannimi.messenger.adapter.out.persistence.entity.OutboxEventJpaEntity;
import com.jeannimi.messenger.domain.outbox.OutboxEvent;
import org.springframework.stereotype.Component;

@Component
public class OutboxEventPersistenceMapper {

  public OutboxEvent toDomain(OutboxEventJpaEntity entity) {

    if (entity == null) {
      return null;
    }

    return OutboxEvent.reconstitute(
        entity.getId(),
        entity.getEventId(),
        entity.getTopic(),
        entity.getEventType(),
        entity.getAggregateId(),
        entity.getStatus(),
        entity.getPayload(),
        entity.getCreatedAt());
  }

  public OutboxEventJpaEntity toEntity(OutboxEvent event) {

    if (event == null) {
      return null;
    }

    return new OutboxEventJpaEntity(
        event.getId(),
        event.getEventId(),
        event.getTopic(),
        event.getEventType(),
        event.getAggregateId(),
        event.getStatus(),
        event.getPayload(),
        event.getCreatedAt());
  }
}
