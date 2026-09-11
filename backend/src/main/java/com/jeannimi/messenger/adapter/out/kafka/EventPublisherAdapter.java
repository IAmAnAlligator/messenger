package com.jeannimi.messenger.adapter.out.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeannimi.messenger.application.event.ApplicationEvent;
import com.jeannimi.messenger.application.event.EventType;
import com.jeannimi.messenger.application.port.out.EventPublisherPort;
import com.jeannimi.messenger.common.exception_handling.OutboxException;
import com.jeannimi.messenger.adapter.kafka.envelope.KafkaEventEnvelope;
import com.jeannimi.messenger.adapter.out.kafka.mapper.KafkaEventMapper;
import com.jeannimi.messenger.adapter.out.kafka.mapper.KafkaEventTypeMapper;
import com.jeannimi.messenger.adapter.out.kafka.mapper.KafkaTopicMapper;
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
  private final KafkaEventTypeMapper kafkaEventTypeMapper;
  private final KafkaTopicMapper kafkaTopicMapper;

  @Override
  public void publish(EventType type, String aggregateId, ApplicationEvent event) {

    try {
      UUID eventId = UUID.randomUUID();

      var kafkaEventType = kafkaEventTypeMapper.toKafkaEventType(type);

      var kafkaEvent = kafkaEventMapper.toKafkaEvent(type, event);

      KafkaEventEnvelope envelope =
          new KafkaEventEnvelope(
              eventId, kafkaEventType.name(), aggregateId, objectMapper.valueToTree(kafkaEvent));

      outboxService.saveEvent(
          kafkaTopicMapper.toTopic(type),
          kafkaEventType.name(),
          aggregateId,
          objectMapper.writeValueAsString(envelope));

    } catch (JsonProcessingException e) {
      throw new OutboxException("Failed to serialize event", e);
    }
  }
}
