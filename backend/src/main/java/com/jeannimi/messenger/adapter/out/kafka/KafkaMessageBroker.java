package com.jeannimi.messenger.adapter.out.kafka;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeannimi.messenger.adapter.kafka.envelope.KafkaEventEnvelope;
import com.jeannimi.messenger.application.port.out.MessageBrokerPort;
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
  public void publish(
      String topic,
      UUID eventId,
      String key,
      String eventType,
      String payload) {

    try {

      KafkaEventEnvelope envelope =
          new KafkaEventEnvelope(
              eventId,
              eventType,
              key,
              objectMapper.readTree(payload));

      String message = objectMapper.writeValueAsString(envelope);

      kafkaTemplate
          .send(topic, key, message)
          .get();

    } catch (JsonProcessingException e) {

      throw new IllegalStateException(
          "Failed to serialize Kafka event",
          e);

    } catch (Exception e) {

      throw new IllegalStateException(
          "Failed to publish message to Kafka",
          e);
    }
  }
}