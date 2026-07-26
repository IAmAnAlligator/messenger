package com.jeannimi.messenger.outbox.publisher;

import com.jeannimi.messenger.kafka.event.EventType;

public interface EventPublisher {

  void publish(
      String topic,
      EventType type,
      String aggregateId,
      Object payload);
}