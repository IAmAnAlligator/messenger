package com.jeannimi.messenger.adapter.out.persistence.mapper;

import com.jeannimi.messenger.adapter.out.persistence.entity.ProcessedEventJpaEntity;
import com.jeannimi.messenger.domain.event.EventId;
import com.jeannimi.messenger.domain.event.ProcessedEvent;
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
}
