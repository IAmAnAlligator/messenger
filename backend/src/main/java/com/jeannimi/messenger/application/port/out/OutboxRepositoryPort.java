package com.jeannimi.messenger.application.port.out;

import com.jeannimi.messenger.application.outbox.OutboxBatchRequest;
import com.jeannimi.messenger.application.outbox.OutboxCreateData;
import com.jeannimi.messenger.application.outbox.OutboxEventData;
import java.time.Instant;
import java.util.List;

public interface OutboxRepositoryPort {

  List<OutboxEventData> findBatch(OutboxBatchRequest request);

  OutboxEventData save(OutboxCreateData event);

  void markSent(Long id);

  void scheduleRetry(Long id, Instant nextAttemptAt);

  void registerFinalFailure(Long id);
}