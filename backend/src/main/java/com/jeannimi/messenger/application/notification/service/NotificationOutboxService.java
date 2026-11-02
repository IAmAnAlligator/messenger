package com.jeannimi.messenger.application.notification.service;

import com.jeannimi.messenger.application.notification.dto.MessageNotification;
import com.jeannimi.messenger.application.port.out.IdGenerator;
import com.jeannimi.messenger.application.port.out.NotificationOutboxRepositoryPort;
import com.jeannimi.messenger.domain.notification.NotificationChannel;
import com.jeannimi.messenger.domain.notification.NotificationOutbox;
import com.jeannimi.messenger.domain.notification.NotificationOutboxId;
import com.jeannimi.messenger.domain.user.UserId;
import java.time.Instant;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NotificationOutboxService {

  private final IdGenerator idGenerator;
  private final NotificationOutboxRepositoryPort notificationOutboxRepository;

  @Transactional
  public void enqueue(MessageNotification notification) {
    enqueue(notification, NotificationChannel.WEB_PUSH);
  }

  @Transactional
  public void enqueue(
      MessageNotification notification,
      NotificationChannel channel
  ) {
    Objects.requireNonNull(notification, "notification");
    Objects.requireNonNull(channel, "channel");

    Instant now = Instant.now();

    List<NotificationOutbox> tasks =
        notification.recipientUserIds().stream()
            .filter(Objects::nonNull)
            .filter(userId -> !userId.equals(notification.senderId()))
            .distinct()
            .map(userId -> createTask(notification, userId, channel, now))
            .toList();

    if (tasks.isEmpty()) {
      return;
    }

    notificationOutboxRepository.saveAll(tasks);
  }

  @Transactional
  public List<NotificationOutbox> claimBatch(int batchSize) {
    if (batchSize <= 0) {
      throw new IllegalArgumentException(
          "batchSize must be greater than zero");
    }

    return notificationOutboxRepository.claimBatch(batchSize);
  }

  @Transactional
  public void markAsSent(NotificationOutboxId id) {
    Objects.requireNonNull(id, "id");

    notificationOutboxRepository.markAsSent(id);
  }

  @Transactional
  public void scheduleRetry(
      NotificationOutboxId id,
      Instant nextAttemptAt,
      String error
  ) {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(nextAttemptAt, "nextAttemptAt");
    Objects.requireNonNull(error, "error");

    notificationOutboxRepository.scheduleRetry(
        id,
        nextAttemptAt,
        error
    );
  }

  @Transactional
  public void markAsDead(NotificationOutboxId id, String error) {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(error, "error");

    notificationOutboxRepository.markAsDead(id, error);
  }

  private NotificationOutbox createTask(
      MessageNotification notification,
      UserId recipientUserId,
      NotificationChannel channel,
      Instant now
  ) {
    NotificationOutboxId id =
        new NotificationOutboxId(idGenerator.generate());

    return NotificationOutbox.create(
        id,
        notification.eventId(),
        notification.messageId(),
        notification.chatId(),
        notification.chatType(),
        notification.chatName(),
        recipientUserId,
        notification.senderId(),
        notification.senderUsername(),
        channel,
        now
    );
  }
}