package com.jeannimi.messenger.domain.outbox;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public final class OutboxEvent {

  private final Long id;
  private final UUID eventId;
  private final String topic;
  private final String eventType;
  private final String aggregateId;
  private final OutboxStatus status;
  private final String payload;
  private final Instant createdAt;
  private final int attemptCount;
  private final Instant nextAttemptAt;

  private OutboxEvent(
      Long id,
      UUID eventId,
      String topic,
      String eventType,
      String aggregateId,
      OutboxStatus status,
      String payload,
      Instant createdAt,
      int attemptCount,
      Instant nextAttemptAt) {

    this.id = id;
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
      UUID eventId,
      String topic,
      String eventType,
      String aggregateId,
      String payload) {

    Instant now = Instant.now();

    return new OutboxEvent(
        null,
        eventId,
        topic,
        eventType,
        aggregateId,
        OutboxStatus.NEW,
        payload,
        now,
        0,
        now);
  }

  public static OutboxEvent reconstitute(
      Long id,
      UUID eventId,
      String topic,
      String eventType,
      String aggregateId,
      OutboxStatus status,
      String payload,
      Instant createdAt,
      int attemptCount,
      Instant nextAttemptAt) {

    return new OutboxEvent(
        Objects.requireNonNull(id, "id"),
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

    return id != null && id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}