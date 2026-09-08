package com.jeannimi.messenger.domain.message;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public final class ProcessedMessage {

  private final UUID eventId;
  private final Instant processedAt;

  private ProcessedMessage(UUID eventId, Instant processedAt) {

    this.eventId = Objects.requireNonNull(eventId, "eventId");
    this.processedAt = Objects.requireNonNull(processedAt, "processedAt");
  }

  public static ProcessedMessage create(UUID eventId) {

    return new ProcessedMessage(eventId, Instant.now());
  }

  public static ProcessedMessage reconstitute(UUID eventId, Instant processedAt) {

    return new ProcessedMessage(eventId, processedAt);
  }

  @Override
  public boolean equals(Object o) {

    if (this == o) {
      return true;
    }

    if (!(o instanceof ProcessedMessage that)) {
      return false;
    }

    return eventId.equals(that.eventId);
  }

  @Override
  public int hashCode() {
    return eventId.hashCode();
  }
}
