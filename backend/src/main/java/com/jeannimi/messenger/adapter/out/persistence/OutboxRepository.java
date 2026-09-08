package com.jeannimi.messenger.adapter.out.persistence;

import com.jeannimi.messenger.adapter.out.persistence.entity.OutboxEventJpaEntity;
import com.jeannimi.messenger.adapter.out.persistence.mapper.OutboxEventPersistenceMapper;
import com.jeannimi.messenger.application.outbox.OutboxBatchRequest;
import com.jeannimi.messenger.application.outbox.OutboxEventData;
import com.jeannimi.messenger.application.outbox.OutboxStatus;
import com.jeannimi.messenger.application.port.out.OutboxRepositoryPort;
import com.jeannimi.messenger.domain.outbox.OutboxEvent;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class OutboxRepository implements OutboxRepositoryPort {

  private final OutboxJpaRepository outboxJpaRepository;
  private final OutboxEventPersistenceMapper outboxEventPersistenceMapper;

  @Override
  @Transactional(readOnly = true)
  public List<OutboxEventData> findBatch(OutboxBatchRequest request) {

    return outboxJpaRepository
        .findBatch(toDomainStatus(request.status()), PageRequest.of(0, request.limit()))
        .stream()
        .map(outboxEventPersistenceMapper::toDomain)
        .map(this::toData)
        .toList();
  }

  @Override
  @Transactional
  public OutboxEventData save(OutboxEventData event) {

    OutboxEvent domainEvent =
        OutboxEvent.create(
            event.eventId(),
            event.topic(),
            event.eventType(),
            event.aggregateId(),
            event.payload());

    OutboxEventJpaEntity entity = outboxEventPersistenceMapper.toEntity(domainEvent);

    OutboxEventJpaEntity saved = outboxJpaRepository.save(entity);

    return toData(outboxEventPersistenceMapper.toDomain(saved));
  }

  @Override
  @Transactional
  public void markSent(Long id) {
    outboxJpaRepository.markSent(id);
  }

  @Override
  @Transactional
  public void markFailed(Long id) {
    outboxJpaRepository.markFailed(id);
  }

  private OutboxEventData toData(OutboxEvent event) {

    return new OutboxEventData(
        event.getId(),
        event.getEventId(),
        event.getTopic(),
        event.getEventType(),
        event.getAggregateId(),
        event.getPayload());
  }

  private com.jeannimi.messenger.domain.outbox.OutboxStatus toDomainStatus(OutboxStatus status) {

    return switch (status) {
      case NEW -> com.jeannimi.messenger.domain.outbox.OutboxStatus.NEW;
      case SENT -> com.jeannimi.messenger.domain.outbox.OutboxStatus.SENT;
      case FAILED -> com.jeannimi.messenger.domain.outbox.OutboxStatus.FAILED;
    };
  }
}
