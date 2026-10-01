package com.jeannimi.messenger.adapter.out.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeannimi.messenger.adapter.kafka.envelope.KafkaEventEnvelope;
import com.jeannimi.messenger.application.port.out.MessageBrokerPort;
import com.jeannimi.messenger.domain.event.EventId;
import com.jeannimi.messenger.domain.outbox.AggregateId;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaMessageBroker implements MessageBrokerPort {

  private final KafkaTemplate<String, String> kafkaTemplate;
  private final ObjectMapper objectMapper;

  @Override
  public void publish(String topic, EventId eventId, AggregateId aggregateId, String eventType, String payload) {

    try {

      UUID aggregateIdValue = aggregateId.value();

      KafkaEventEnvelope envelope =
          new KafkaEventEnvelope(eventId.value(), eventType, aggregateIdValue, objectMapper.readTree(payload));

      String message = objectMapper.writeValueAsString(envelope);

      kafkaTemplate.send(topic, aggregateIdValue.toString(), message).get();

    } catch (JsonProcessingException e) {

      throw new IllegalStateException("Failed to serialize Kafka event", e);

    } catch (Exception e) {

      throw new IllegalStateException("Failed to publish message to Kafka", e);
    }
  }
}
