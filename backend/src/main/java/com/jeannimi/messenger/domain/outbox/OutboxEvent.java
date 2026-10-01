package com.jeannimi.messenger.domain.outbox;

import com.jeannimi.messenger.domain.event.EventId;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;

@Getter
public final class OutboxEvent {

  private final OutboxEventId id;
  private final EventId eventId;
  private final String topic;
  private final String eventType;
  private final AggregateId aggregateId;
  private final OutboxStatus status;
  private final String payload;
  private final Instant createdAt;
  private final int attemptCount;
  private final Instant nextAttemptAt;

  private OutboxEvent(
      OutboxEventId id,
      EventId eventId,
      String topic,
      String eventType,
      AggregateId aggregateId,
      OutboxStatus status,
      String payload,
      Instant createdAt,
      int attemptCount,
      Instant nextAttemptAt) {

    this.id = Objects.requireNonNull(id, "outboxEventId");
    this.eventId = Objects.requireNonNull(eventId, "eventId");
    this.topic = Objects.requireNonNull(topic, "topic");
    this.eventType = Objects.requireNonNull(eventType, "eventType");
    this.aggregateId = Objects.requireNonNull(aggregateId, "aggregateId");
    this.status = Objects.requireNonNull(status, "status");
    this.payload = Objects.requireNonNull(payload, "payload");
    this.createdAt = Objects.requireNonNull(createdAt, "createdAt");

    if (attemptCount < 0) {
      throw new IllegalArgumentException("Attempt count cannot be negative");
    }

    this.attemptCount = attemptCount;
    this.nextAttemptAt = Objects.requireNonNull(nextAttemptAt, "nextAttemptAt");
  }

  public static OutboxEvent create(
      OutboxEventId id, EventId eventId, String topic, String eventType, AggregateId aggregateId, String payload) {

    Instant now = Instant.now();

    return new OutboxEvent(
        id, eventId, topic, eventType, aggregateId, OutboxStatus.NEW, payload, now, 0, now);
  }

  public static OutboxEvent reconstitute(
      OutboxEventId id,
      EventId eventId,
      String topic,
      String eventType,
      AggregateId aggregateId,
      OutboxStatus status,
      String payload,
      Instant createdAt,
      int attemptCount,
      Instant nextAttemptAt) {

    return new OutboxEvent(
        id,
        eventId,
        topic,
        eventType,
        aggregateId,
        status,
        payload,
        createdAt,
        attemptCount,
        nextAttemptAt);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof OutboxEvent that)) {
      return false;
    }

    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
