package com.jeannimi.messenger.adapter.out.persistence.adapter;

import com.jeannimi.messenger.adapter.out.persistence.entity.NotificationOutboxJpaEntity;
import com.jeannimi.messenger.adapter.out.persistence.repository.NotificationOutboxJpaRepository;
import com.jeannimi.messenger.application.port.out.NotificationOutboxRepositoryPort;
import com.jeannimi.messenger.domain.chat.ChatId;
import com.jeannimi.messenger.domain.event.EventId;
import com.jeannimi.messenger.domain.message.MessageId;
import com.jeannimi.messenger.domain.notification.NotificationOutbox;
import com.jeannimi.messenger.domain.notification.NotificationOutboxId;
import com.jeannimi.messenger.domain.user.UserId;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class NotificationOutboxPersistenceAdapter
    implements NotificationOutboxRepositoryPort {

  private final NotificationOutboxJpaRepository repository;

  @Override
  public void saveAll(List<NotificationOutbox> tasks) {
    List<NotificationOutboxJpaEntity> entities =
        tasks.stream()
            .map(this::toEntity)
            .toList();

    if (entities.isEmpty()) {
      return;
    }

    repository.saveAll(entities);
  }

  @Override
  public List<NotificationOutbox> claimBatch(int batchSize) {
    List<NotificationOutboxJpaEntity> entities =
        repository.findAvailableBatch(batchSize);

    if (entities.isEmpty()) {
      return List.of();
    }

    List<UUID> ids = entities.stream()
        .map(NotificationOutboxJpaEntity::getId)
        .toList();

    int updatedCount = repository.markBatchAsProcessing(ids);

    if (updatedCount != ids.size()) {
      throw new IllegalStateException(
          "Not all notification outbox tasks were claimed");
    }

    // После clearAutomatically сущности нужно загрузить заново.
    Map<UUID, NotificationOutboxJpaEntity> updatedEntities =
        repository.findAllById(ids).stream()
            .collect(Collectors.toMap(
                NotificationOutboxJpaEntity::getId,
                Function.identity()));

    // Сохраняем исходный порядок выборки.
    return ids.stream()
        .map(updatedEntities::get)
        .map(this::toDomain)
        .toList();
  }

  @Override
  public void markAsSent(NotificationOutboxId id) {
    int updatedCount = repository.markAsSent(id.value());

    if (updatedCount != 1) {
      throw new IllegalStateException(
          "Notification outbox task could not be marked as sent: " + id.value());
    }
  }

  @Override
  public void scheduleRetry(
      NotificationOutboxId id,
      Instant nextAttemptAt,
      String error
  ) {
    int updatedCount = repository.scheduleRetry(
        id.value(),
        nextAttemptAt,
        error
    );

    if (updatedCount != 1) {
      throw new IllegalStateException(
          "Notification outbox task could not be scheduled for retry: "
              + id.value()
      );
    }
  }

  @Override
  public void markAsDead(NotificationOutboxId id, String error) {
    int updatedCount = repository.markAsDead(id.value(), error);

    if (updatedCount != 1) {
      throw new IllegalStateException(
          "Notification outbox task could not be marked as dead: "
              + id.value()
      );
    }
  }

  private NotificationOutboxJpaEntity toEntity(NotificationOutbox task) {
    return new NotificationOutboxJpaEntity(
        task.getId().value(),
        task.getEventId().value(),
        task.getMessageId().value(),
        task.getChatId().value(),
        task.getRecipientUserId().value(),
        task.getSenderUserId().value(),
        task.getSenderUsername(),
        task.getChannel(),
        task.getCreatedAt()
    );
  }

  private NotificationOutbox toDomain(NotificationOutboxJpaEntity entity) {
    return NotificationOutbox.reconstitute(
        new NotificationOutboxId(entity.getId()),
        new EventId(entity.getEventId()),
        new MessageId(entity.getMessageId()),
        new ChatId(entity.getChatId()),
        new UserId(entity.getRecipientUserId()),
        new UserId(entity.getSenderUserId()),
        entity.getSenderUsername(),
        entity.getChannel(),
        entity.getStatus(),
        entity.getAttempts(),
        entity.getNextAttemptAt(),
        entity.getLockedUntil(),
        entity.getCreatedAt(),
        entity.getSentAt(),
        entity.getLastError());
  }
}