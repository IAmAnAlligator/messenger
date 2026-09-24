package com.jeannimi.messenger.adapter.out.persistence;

import com.jeannimi.messenger.adapter.out.persistence.entity.OutboxEventJpaEntity;
import com.jeannimi.messenger.adapter.out.persistence.mapper.OutboxEventPersistenceMapper;
import com.jeannimi.messenger.application.outbox.OutboxBatchRequest;
import com.jeannimi.messenger.application.outbox.OutboxCreateData;
import com.jeannimi.messenger.application.outbox.OutboxEventData;
import com.jeannimi.messenger.application.outbox.OutboxStatus;
import com.jeannimi.messenger.application.port.out.OutboxRepositoryPort;
import com.jeannimi.messenger.domain.outbox.OutboxEvent;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OutboxRepository implements OutboxRepositoryPort {

  private final OutboxJpaRepository outboxJpaRepository;
  private final OutboxEventPersistenceMapper outboxEventPersistenceMapper;

  @Override
  public List<OutboxEventData> findBatch(
      OutboxBatchRequest request) {

    return outboxJpaRepository
        .findBatch(
            toDomainStatus(request.status()),
            PageRequest.of(0, request.limit()))
        .stream()
        .map(outboxEventPersistenceMapper::toDomain)
        .map(this::toData)
        .toList();
  }

  @Override
  public OutboxEventData save(OutboxCreateData event) {

    OutboxEvent domainEvent =
        OutboxEvent.create(
            event.eventId(),
            event.topic(),
            event.eventType(),
            event.aggregateId(),
            event.payload());

    OutboxEventJpaEntity entity =
        outboxEventPersistenceMapper.toEntity(domainEvent);

    OutboxEventJpaEntity saved =
        outboxJpaRepository.save(entity);

    return toData(
        outboxEventPersistenceMapper.toDomain(saved));
  }

  @Override
  public void markSent(Long id) {
    outboxJpaRepository.markSent(id);
  }

  @Override
  public void scheduleRetry(
      Long id,
      Instant nextAttemptAt) {

    outboxJpaRepository.scheduleRetry(
        id,
        nextAttemptAt);
  }

  @Override
  public void registerFinalFailure(Long id) {
    outboxJpaRepository.registerFinalFailure(id);
  }

  private OutboxEventData toData(
      OutboxEvent event) {

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

  private com.jeannimi.messenger.domain.outbox.OutboxStatus
  toDomainStatus(OutboxStatus status) {

    return com.jeannimi.messenger.domain.outbox.OutboxStatus
        .valueOf(status.name());
  }
}