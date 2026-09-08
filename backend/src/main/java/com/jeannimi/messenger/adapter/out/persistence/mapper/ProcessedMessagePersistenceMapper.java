package com.jeannimi.messenger.adapter.out.persistence.mapper;

import com.jeannimi.messenger.adapter.out.persistence.entity.ProcessedMessageJpaEntity;
import com.jeannimi.messenger.domain.message.ProcessedMessage;
import org.springframework.stereotype.Component;

@Component
public class ProcessedMessagePersistenceMapper {

  public ProcessedMessage toDomain(ProcessedMessageJpaEntity entity) {

    if (entity == null) {
      return null;
    }

    return ProcessedMessage.reconstitute(entity.getEventId(), entity.getProcessedAt());
  }

  public ProcessedMessageJpaEntity toEntity(ProcessedMessage processedMessage) {

    if (processedMessage == null) {
      return null;
    }

    return new ProcessedMessageJpaEntity(
        processedMessage.getEventId(), processedMessage.getProcessedAt());
  }
}
