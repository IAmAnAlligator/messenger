package com.jeannimi.messenger.adapter.out.persistence;

import com.jeannimi.messenger.application.outbox.OutboxBatchRequest;
import com.jeannimi.messenger.application.outbox.OutboxEventData;
import com.jeannimi.messenger.application.outbox.OutboxStatus;
import com.jeannimi.messenger.application.port.out.OutboxRepositoryPort;
import com.jeannimi.messenger.outbox.entity.OutboxEvent;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class OutboxRepository implements OutboxRepositoryPort {

  private final OutboxJpaRepository outboxJpaRepository;

  @Override
  public List<OutboxEventData> findBatch(OutboxBatchRequest request) {

    OutboxStatus status = request.status();

    return outboxJpaRepository
        .findBatch(
            OutboxStatusMapper.toEntity(status),
            PageRequest.of(0, request.limit()))
        .stream()
        .map(OutboxEventMapper::toData)
        .toList();
  }

  @Override
  public OutboxEventData save(OutboxEventData event) {

    OutboxEvent entity = OutboxEventMapper.toEntity(event);

    OutboxEvent saved = outboxJpaRepository.save(entity);

    return OutboxEventMapper.toData(saved);
  }

  @Override
  public void markSent(Long id) {
    outboxJpaRepository.markSent(id);
  }

  @Override
  public void markFailed(Long id) {
    outboxJpaRepository.markFailed(id);
  }
}