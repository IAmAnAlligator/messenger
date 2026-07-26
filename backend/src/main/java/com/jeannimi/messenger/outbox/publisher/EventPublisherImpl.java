package com.jeannimi.messenger.outbox.publisher;

import com.jeannimi.messenger.kafka.event.EventType;
import com.jeannimi.messenger.outbox.service.OutboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EventPublisherImpl implements EventPublisher {

  private final OutboxService outboxService;

  @Override
  public void publish(
      String topic,
      EventType type,
      String aggregateId,
      Object payload) {

    outboxService.saveEvent(
        topic,
        type,
        aggregateId,
        payload);
  }
}