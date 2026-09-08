package com.jeannimi.messenger.outbox.scheduler;

import com.jeannimi.messenger.application.outbox.OutboxBatchRequest;
import com.jeannimi.messenger.application.outbox.OutboxEventData;
import com.jeannimi.messenger.application.outbox.OutboxStatus;
import com.jeannimi.messenger.application.port.out.MessageBrokerPort;
import com.jeannimi.messenger.application.port.out.OutboxRepositoryPort;
import com.jeannimi.messenger.outbox.service.OutboxStatusService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

  private static final int BATCH_SIZE = 100;

  private final OutboxRepositoryPort outboxRepository;
  private final OutboxStatusService outboxStatusService;
  private final MessageBrokerPort messagePublisher;

  @Scheduled(fixedDelay = 1000)
  public void publishOutboxEvents() {

    List<OutboxEventData> events =
        outboxRepository.findBatch(new OutboxBatchRequest(OutboxStatus.NEW, BATCH_SIZE));

    for (OutboxEventData event : events) {

      try {

        messagePublisher.publish(event.topic(), event.aggregateId(), event.payload());

        outboxStatusService.markSent(event.id());

        log.info("[OUTBOX SENT] id={}, type={}", event.id(), event.eventType());

      } catch (Exception e) {

        outboxStatusService.markFailed(event.id());

        log.error("[OUTBOX FAILED] id={}", event.id(), e);
      }
    }
  }
}
