package com.jeannimi.messenger.outbox.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeannimi.messenger.common.exception_handling.OutboxException;
import com.jeannimi.messenger.kafka.envelope.KafkaEventEnvelope;
import com.jeannimi.messenger.kafka.event.EventType;
import com.jeannimi.messenger.outbox.entity.OutboxEvent;
import com.jeannimi.messenger.outbox.repository.OutboxRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OutboxService {

  private final OutboxRepository outboxRepository;
  private final ObjectMapper objectMapper;

  @Transactional
  public void saveEvent(String topic, EventType eventType, String aggregateId, Object event) {

    validate(topic, eventType, event);

    try {

      UUID eventId = UUID.randomUUID();

      KafkaEventEnvelope envelope =
          new KafkaEventEnvelope(
              eventId, eventType.name(), aggregateId, objectMapper.valueToTree(event));

      outboxRepository.save(
          OutboxEvent.pending(
              eventId,
              topic,
              eventType.name(),
              aggregateId,
              objectMapper.writeValueAsString(envelope)));

    } catch (JsonProcessingException e) {

      throw new OutboxException("Failed to serialize event", e);
    }
  }

  private void validate(String topic, EventType eventType, Object event) {

    if (topic == null || topic.isBlank()) {
      throw new IllegalArgumentException("Topic is empty");
    }

    if (eventType == null) {
      throw new IllegalArgumentException("Event type is null");
    }

    if (event == null) {
      throw new IllegalArgumentException("Event is null");
    }
  }
}
