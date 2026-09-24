package com.jeannimi.messenger.application.outbox.service;

import com.jeannimi.messenger.application.outbox.OutboxBatchRequest;
import com.jeannimi.messenger.application.outbox.OutboxEventData;
import com.jeannimi.messenger.application.outbox.OutboxStatus;
import com.jeannimi.messenger.application.port.out.MessageBrokerPort;
import com.jeannimi.messenger.application.port.out.OutboxRepositoryPort;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPublishService {

  private static final int BATCH_SIZE = 100;

  private final OutboxRepositoryPort outboxRepository;
  private final OutboxStatusService outboxStatusService;
  private final MessageBrokerPort messageBroker;

  public void publishBatch() {

    List<OutboxEventData> events =
        outboxRepository.findBatch(
            new OutboxBatchRequest(
                OutboxStatus.NEW,
                BATCH_SIZE));

    for (OutboxEventData event : events) {
      publish(event);
    }
  }

  private void publish(OutboxEventData event) {

    try {

      messageBroker.publish(
          event.topic(),
          event.eventId(),
          event.aggregateId(),
          event.eventType(),
          event.payload());

      outboxStatusService.markSent(event.id());

      log.info(
          "[OUTBOX SENT] id={}, eventId={}, type={}",
          event.id(),
          event.eventId(),
          event.eventType());

    } catch (Exception e) {

      int nextAttempt = event.attemptCount() + 1;

      outboxStatusService.handleFailure(
          event.id(),
          event.attemptCount());

      log.warn(
          "[OUTBOX PUBLISH FAILED] id={}, eventId={}, type={}, attempt={}",
          event.id(),
          event.eventId(),
          event.eventType(),
          nextAttempt,
          e);
    }
  }
}