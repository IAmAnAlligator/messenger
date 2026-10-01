package com.jeannimi.messenger.application.outbox.service;

import com.jeannimi.messenger.application.outbox.OutboxCreateData;
import com.jeannimi.messenger.application.port.out.IdGenerator;
import com.jeannimi.messenger.application.port.out.OutboxRepositoryPort;
import com.jeannimi.messenger.domain.common.DomainId;
import com.jeannimi.messenger.domain.event.EventId;
import com.jeannimi.messenger.domain.outbox.AggregateId;
import com.jeannimi.messenger.domain.outbox.OutboxEventId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OutboxService {

  private final OutboxRepositoryPort outboxRepository;
  private final IdGenerator idGenerator;

  @Transactional
  public void saveEvent(
      String topic,
      String eventType,
      AggregateId aggregateId,
      String payload) {

    validate(topic, eventType, aggregateId, payload);

    OutboxEventId outboxEventId = new OutboxEventId(idGenerator.generate());
    EventId eventId = new EventId(idGenerator.generate());

    outboxRepository.save(
        new OutboxCreateData(
            outboxEventId,
            eventId,
            topic,
            eventType,
            aggregateId,
            payload));
  }

  private void validate(
      String topic,
      String eventType,
      DomainId aggregateId,
      String payload) {

    if (topic == null || topic.isBlank()) {
      throw new IllegalArgumentException("Topic is empty");
    }

    if (eventType == null || eventType.isBlank()) {
      throw new IllegalArgumentException("Event type is empty");
    }

    if (aggregateId == null || aggregateId.value().toString().isBlank()) {
      throw new IllegalArgumentException("Aggregate id is empty");
    }

    if (payload == null || payload.isBlank()) {
      throw new IllegalArgumentException("Payload is empty");
    }
  }
}