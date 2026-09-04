package com.jeannimi.messenger.adapter.out.kafka;

import com.jeannimi.messenger.application.port.out.MessageBrokerPort;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class KafkaMessageBroker implements MessageBrokerPort {

  private final KafkaTemplate<String, String> kafkaTemplate;

  @Override
  public void publish(
      String topic,
      String key,
      String payload) {

    try {
      kafkaTemplate
          .send(topic, key, payload)
          .get();

    } catch (Exception e) {
      throw new IllegalStateException(
          "Failed to publish message to Kafka",
          e);
    }
  }
}