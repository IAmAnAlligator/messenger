package com.jeannimi.messenger.service;

import com.jeannimi.messenger.entity.OutboxEvent;
import com.jeannimi.messenger.entity.OutboxStatus;
import com.jeannimi.messenger.repository.OutboxRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class OutboxPublisher {

  private final OutboxRepository outboxRepository;

  private final KafkaTemplate<String, String> kafkaTemplate;

  @Scheduled(fixedDelay = 1000)
  @Transactional
  public void publishOutboxEvents() {

    List<OutboxEvent> events = outboxRepository.findByStatus(OutboxStatus.NEW);

    for (OutboxEvent event : events) {

      try {

        kafkaTemplate.send(event.getTopic(), event.getPayload()).get();

        event.setStatus(OutboxStatus.SENT);

      } catch (Exception e) {

        log.error("Failed to publish outbox event {}", event.getId(), e);

        event.setStatus(OutboxStatus.FAILED);
      }
    }
  }
}
