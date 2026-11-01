package com.jeannimi.messenger.application.port.out;

import com.jeannimi.messenger.domain.notification.NotificationOutbox;
import com.jeannimi.messenger.domain.notification.NotificationOutboxId;
import java.time.Instant;
import java.util.List;

public interface NotificationOutboxRepositoryPort {

  void saveAll(List<NotificationOutbox> tasks);


  List<NotificationOutbox> claimBatch(int batchSize);

  void markAsSent(NotificationOutboxId id);

  void scheduleRetry(
      NotificationOutboxId id,
      Instant nextAttemptAt,
      String error
  );

  void markAsDead(
      NotificationOutboxId id,
      String error
  );
}