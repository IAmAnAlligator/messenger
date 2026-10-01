package com.jeannimi.messenger.application.port.out;

import com.jeannimi.messenger.application.outbox.OutboxBatchRequest;
import com.jeannimi.messenger.application.outbox.OutboxCreateData;
import com.jeannimi.messenger.application.outbox.OutboxEventData;
import com.jeannimi.messenger.domain.outbox.OutboxEventId;
import java.time.Instant;
import java.util.List;

public interface OutboxRepositoryPort {

  List<OutboxEventData> findBatch(OutboxBatchRequest request);

  OutboxEventData save(OutboxCreateData event);

  void markSent(OutboxEventId id);

  void scheduleRetry(OutboxEventId id, Instant nextAttemptAt);

  void registerFinalFailure(OutboxEventId id);
}
