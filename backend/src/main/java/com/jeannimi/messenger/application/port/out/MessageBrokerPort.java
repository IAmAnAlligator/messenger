package com.jeannimi.messenger.application.port.out;

import java.util.UUID;

public interface MessageBrokerPort {

  void publish(
      String topic,
      UUID eventId,
      String key,
      String eventType,
      String payload);
}
