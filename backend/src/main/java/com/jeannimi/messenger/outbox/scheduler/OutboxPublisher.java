package com.jeannimi.messenger.outbox.scheduler;

import com.jeannimi.messenger.outbox.entity.OutboxEvent;
import com.jeannimi.messenger.outbox.entity.OutboxStatus;
import com.jeannimi.messenger.outbox.repository.OutboxRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

  private final OutboxRepository outboxRepository;

  private final KafkaTemplate<String, String> kafkaTemplate;

  @Scheduled(fixedDelay = 1000)
  @Transactional
  public void publishOutboxEvents() {

    List<OutboxEvent> events = outboxRepository.findBatch(OutboxStatus.NEW, PageRequest.of(0, 100));

    for (OutboxEvent event : events) {

      try {

        kafkaTemplate.send(event.getTopic(), event.getAggregateId(), event.getPayload()).get();

        event.markSent();

        log.info("[OUTBOX SENT] id={}, type={}", event.getId(), event.getEventType());

      } catch (Exception e) {

        event.markFailed();

        log.error("[OUTBOX FAILED] id={}", event.getId(), e);
      }
    }
  }
}
