package com.jeannimi.messenger.adapter.out.persistence.mapper;

import com.jeannimi.messenger.adapter.out.persistence.entity.ProcessedEventJpaEntity;
import com.jeannimi.messenger.application.outbox.OutboxCreateData;
import com.jeannimi.messenger.application.outbox.OutboxEventData;
import com.jeannimi.messenger.domain.event.EventId;
import com.jeannimi.messenger.domain.event.ProcessedEvent;
import com.jeannimi.messenger.domain.outbox.OutboxEvent;
import org.springframework.stereotype.Component;

@Component
public class ProcessedEventPersistenceMapper {

  public ProcessedEvent toDomain(ProcessedEventJpaEntity entity) {

    if (entity == null) {
      return null;
    }

    return ProcessedEvent.reconstitute(new EventId(entity.getEventId()), entity.getProcessedAt());
  }

  public ProcessedEventJpaEntity toEntity(ProcessedEvent processedEvent) {

    if (processedEvent == null) {
      return null;
    }

    return new ProcessedEventJpaEntity(
        processedEvent.getId().value(), processedEvent.getProcessedAt());
  }

  public static final class OutboxEventPersistenceMapper {

    private OutboxEventPersistenceMapper() {}

    public static OutboxEvent toDomain(OutboxCreateData data) {
      return OutboxEvent.create(
          data.id(),
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
}
