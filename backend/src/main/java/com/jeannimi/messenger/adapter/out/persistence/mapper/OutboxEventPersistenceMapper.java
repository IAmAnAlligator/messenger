package com.jeannimi.messenger.adapter.out.persistence.mapper;

import com.jeannimi.messenger.adapter.out.persistence.entity.OutboxEventJpaEntity;
import com.jeannimi.messenger.domain.event.EventId;
import com.jeannimi.messenger.domain.outbox.AggregateId;
import com.jeannimi.messenger.domain.outbox.OutboxEvent;
import com.jeannimi.messenger.domain.outbox.OutboxEventId;
import org.springframework.stereotype.Component;

@Component
public class OutboxEventPersistenceMapper {

  public OutboxEvent toDomain(OutboxEventJpaEntity entity) {

    if (entity == null) {
      return null;
    }

    return OutboxEvent.reconstitute(
        new OutboxEventId(entity.getId()),
        new EventId(entity.getEventId()),
        entity.getTopic(),
        entity.getEventType(),
        new AggregateId(entity.getAggregateId()),
        entity.getStatus(),
        entity.getPayload(),
        entity.getCreatedAt(),
        entity.getAttemptCount(),
        entity.getNextAttemptAt());
  }

  public OutboxEventJpaEntity toEntity(OutboxEvent event) {

    if (event == null) {
      return null;
    }

    return new OutboxEventJpaEntity(
        event.getId().value(),
        event.getEventId().value(),
        event.getTopic(),
        event.getEventType(),
        event.getAggregateId().value(),
        event.getStatus(),
        event.getPayload(),
        event.getCreatedAt(),
        event.getAttemptCount(),
        event.getNextAttemptAt());
  }
}
