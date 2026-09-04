package com.jeannimi.messenger.application.port.out;

import com.jeannimi.messenger.application.outbox.OutboxBatchRequest;
import com.jeannimi.messenger.application.outbox.OutboxEventData;
import java.util.List;

public interface OutboxRepositoryPort {

  List<OutboxEventData> findBatch(OutboxBatchRequest request);

  OutboxEventData save(OutboxEventData event);

  void markSent(Long id);

  void markFailed(Long id);
}