package com.jeannimi.messenger.application.port.out;

import com.jeannimi.messenger.domain.event.EventId;
import com.jeannimi.messenger.domain.outbox.AggregateId;

public interface MessageBrokerPort {

  void publish(String topic, EventId eventId, AggregateId key, String eventType, String payload);
}
