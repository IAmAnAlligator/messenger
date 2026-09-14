package com.jeannimi.messenger.adapter.out.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeannimi.messenger.adapter.kafka.envelope.KafkaEventEnvelope;
import com.jeannimi.messenger.adapter.out.kafka.mapper.KafkaEventMapper;
import com.jeannimi.messenger.adapter.out.kafka.mapper.KafkaTopicMapper;
import com.jeannimi.messenger.application.event.ApplicationEvent;
import com.jeannimi.messenger.application.event.EventType;
import com.jeannimi.messenger.application.event.MessageCreatedEvent;
import com.jeannimi.messenger.application.port.out.EventPublisherPort;
import com.jeannimi.messenger.application.outbox.service.OutboxService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventPublisherAdapter implements EventPublisherPort {

  private final OutboxService outboxService;
  private final ObjectMapper objectMapper;
  private final KafkaEventMapper kafkaEventMapper;
  private final KafkaTopicMapper kafkaTopicMapper;

  @Override
  public void publish(
      EventType type,
      String aggregateId,
      ApplicationEvent event) {

    try {
      UUID eventId = UUID.randomUUID();

      JsonNode payload = toKafkaPayload(type, event);

      KafkaEventEnvelope envelope =
          new KafkaEventEnvelope(
              eventId,
              type.name(),
              aggregateId,
              payload);

      outboxService.saveEvent(
          kafkaTopicMapper.toTopic(type),
          envelope.eventType(),
          aggregateId,
          objectMapper.writeValueAsString(envelope));

    } catch (JsonProcessingException e) {
      throw new OutboxException(
          "Failed to serialize event", e);
    }
  }

  private JsonNode toKafkaPayload(
      EventType type,
      ApplicationEvent event) {

    if (type == EventType.MESSAGE_CREATED) {
      MessageCreatedEvent messageCreatedEvent =
          (MessageCreatedEvent) event;

      return objectMapper.valueToTree(
          kafkaEventMapper.toKafkaEvent(messageCreatedEvent));
    }

    return objectMapper.valueToTree(event);
  }
}