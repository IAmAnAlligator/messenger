package com.jeannimi.messenger.adapter.out.persistence.entity;

import com.jeannimi.messenger.domain.outbox.OutboxStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "outbox_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEventJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "event_id", nullable = false, unique = true)
  private UUID eventId;

  @Column(name = "topic", nullable = false)
  private String topic;

  @Column(name = "event_type", nullable = false)
  private String eventType;

  @Column(name = "aggregate_id", nullable = false)
  private String aggregateId;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private OutboxStatus status;

  @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
  private String payload;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  public OutboxEventJpaEntity(
      Long id,
      UUID eventId,
      String topic,
      String eventType,
      String aggregateId,
      OutboxStatus status,
      String payload,
      Instant createdAt) {

    this.id = id;
    this.eventId = eventId;
    this.topic = topic;
    this.eventType = eventType;
    this.aggregateId = aggregateId;
    this.status = status;
    this.payload = payload;
    this.createdAt = createdAt;
  }
}
