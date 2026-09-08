package com.jeannimi.messenger.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "processed_messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProcessedMessageJpaEntity {

  @Id
  @Column(name = "event_id", nullable = false, updatable = false)
  private UUID eventId;

  @Column(name = "processed_at", nullable = false, updatable = false)
  private Instant processedAt;

  public ProcessedMessageJpaEntity(UUID eventId, Instant processedAt) {

    this.eventId = eventId;
    this.processedAt = processedAt;
  }

  @PrePersist
  private void prePersist() {

    if (processedAt == null) {
      processedAt = Instant.now();
    }
  }
}
