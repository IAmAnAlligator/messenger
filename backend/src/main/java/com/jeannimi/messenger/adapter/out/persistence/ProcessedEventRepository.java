package com.jeannimi.messenger.adapter.out.persistence;

import com.jeannimi.messenger.adapter.out.persistence.entity.ProcessedEventJpaEntity;
import com.jeannimi.messenger.adapter.out.persistence.mapper.ProcessedEventPersistenceMapper;
import com.jeannimi.messenger.application.port.out.ProcessedEventRepositoryPort;
import com.jeannimi.messenger.domain.event.EventId;
import com.jeannimi.messenger.domain.event.ProcessedEvent;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class ProcessedEventRepository implements ProcessedEventRepositoryPort {

  private final ProcessedEventJpaRepository processedEventJpaRepository;
  private final ProcessedEventPersistenceMapper processedEventPersistenceMapper;

  @Override
  @Transactional(readOnly = true)
  public boolean existsByEventId(EventId eventId) {

    Objects.requireNonNull(eventId, "eventId");

    return processedEventJpaRepository.existsByEventId(eventId.value());
  }

  @Override
  @Transactional
  public ProcessedEvent save(ProcessedEvent processedEvent) {

    Objects.requireNonNull(processedEvent, "processedEvent");

    ProcessedEventJpaEntity entity = processedEventPersistenceMapper.toEntity(processedEvent);

    ProcessedEventJpaEntity saved = processedEventJpaRepository.save(entity);

    return processedEventPersistenceMapper.toDomain(saved);
  }
}
