package com.jeannimi.messenger.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "outbox_events")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OutboxEvent {

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "topic", nullable = false)
  private String topic;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private OutboxStatus status;

  @Column(name = "payload", nullable = false, columnDefinition = "TEXT")
  private String payload;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @PrePersist
  private void prePersist() {
    if (createdAt == null) {
      createdAt = Instant.now();
    }
  }

  public static OutboxEvent pending(String topic, String payload) {

    OutboxEvent event = new OutboxEvent();

    event.topic = Objects.requireNonNull(topic, "topic");
    event.payload = Objects.requireNonNull(payload, "payload");
    event.status = OutboxStatus.NEW;

    return event;
  }

  public void markSent() {
    status = OutboxStatus.SENT;
  }

  public void markFailed() {
    status = OutboxStatus.FAILED;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof OutboxEvent that)) return false;
    return id != null && id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
