package com.jeannimi.messenger.application.outbox.service;

import com.jeannimi.messenger.application.outbox.OutboxEventData;
import com.jeannimi.messenger.application.port.out.OutboxRepositoryPort;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OutboxService {

  private final OutboxRepositoryPort outboxRepository;

  @Transactional
  public void saveEvent(String topic, String eventType, String aggregateId, String payload) {

    validate(topic, eventType, aggregateId, payload);

    UUID eventId = UUID.randomUUID();

    outboxRepository.save(
        new OutboxEventData(null, eventId, topic, eventType, aggregateId, payload));
  }

  private void validate(String topic, String eventType, String aggregateId, String payload) {

    if (topic == null || topic.isBlank()) {
      throw new IllegalArgumentException("Topic is empty");
    }

    if (eventType == null || eventType.isBlank()) {
      throw new IllegalArgumentException("Event type is empty");
    }

    if (aggregateId == null || aggregateId.isBlank()) {
      throw new IllegalArgumentException("Aggregate id is empty");
    }

    if (payload == null || payload.isBlank()) {
      throw new IllegalArgumentException("Payload is empty");
    }
  }
}
