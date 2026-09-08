package com.jeannimi.messenger.adapter.out.persistence;

import com.jeannimi.messenger.adapter.out.persistence.entity.ProcessedMessageJpaEntity;
import com.jeannimi.messenger.adapter.out.persistence.mapper.ProcessedMessagePersistenceMapper;
import com.jeannimi.messenger.application.port.out.ProcessedMessageRepositoryPort;
import com.jeannimi.messenger.domain.message.ProcessedMessage;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class ProcessedMessageRepository implements ProcessedMessageRepositoryPort {

  private final ProcessedMessageJpaRepository processedMessageJpaRepository;
  private final ProcessedMessagePersistenceMapper processedMessagePersistenceMapper;

  @Override
  @Transactional(readOnly = true)
  public boolean existsByEventId(UUID eventId) {

    return processedMessageJpaRepository.existsByEventId(eventId);
  }

  @Override
  @Transactional
  public ProcessedMessage save(ProcessedMessage processedMessage) {

    ProcessedMessageJpaEntity entity = processedMessagePersistenceMapper.toEntity(processedMessage);

    ProcessedMessageJpaEntity saved = processedMessageJpaRepository.save(entity);

    return processedMessagePersistenceMapper.toDomain(saved);
  }
}
