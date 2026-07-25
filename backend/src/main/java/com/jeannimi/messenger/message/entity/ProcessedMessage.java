package com.jeannimi.messenger.message.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "processed_messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProcessedMessage {

  @Id
  @Column(name = "event_id", nullable = false, updatable = false)
  private UUID eventId;

  @Column(name = "processed_at", nullable = false, updatable = false)
  private Instant processedAt;

  @PrePersist
  private void prePersist() {

    if (processedAt == null) {
      processedAt = Instant.now();
    }
  }

  public static ProcessedMessage of(UUID eventId) {

    ProcessedMessage processed = new ProcessedMessage();

    processed.eventId = Objects.requireNonNull(eventId, "eventId");

    return processed;
  }

  @Override
  public boolean equals(Object o) {

    if (this == o) {
      return true;
    }

    if (!(o instanceof ProcessedMessage that)) {
      return false;
    }

    return eventId != null && eventId.equals(that.eventId);
  }

  @Override
  public int hashCode() {

    return getClass().hashCode();
  }
}
