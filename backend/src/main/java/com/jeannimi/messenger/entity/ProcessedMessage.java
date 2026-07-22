package com.jeannimi.messenger.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;


@Entity
@Table(name = "processed_messages")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProcessedMessage {

  @Id
  @Column(name = "id")
  private Long messageId;

  @Column(name = "processed_at", nullable = false, updatable = false)
  private Instant processedAt;

  @PrePersist
  private void prePersist() {
    if (processedAt == null) {
      processedAt = Instant.now();
    }
  }

  public static ProcessedMessage of(Long messageId) {
    ProcessedMessage processedMessage = new ProcessedMessage();
    processedMessage.messageId = Objects.requireNonNull(messageId, "messageId");

    return processedMessage;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ProcessedMessage that)) return false;
    return messageId != null && messageId.equals(that.messageId);
  }


  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

}