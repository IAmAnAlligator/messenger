package com.jeannimi.messenger.domain.event;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import lombok.Getter;

@Getter
public final class ProcessedEvent {

  private final UUID eventId;
  private final Instant processedAt;

  private ProcessedEvent(UUID eventId, Instant processedAt) {

    this.eventId = Objects.requireNonNull(eventId, "eventId");
    this.processedAt = Objects.requireNonNull(processedAt, "processedAt");
  }

  public static ProcessedEvent create(UUID eventId) {

    return new ProcessedEvent(eventId, Instant.now());
  }

  public static ProcessedEvent reconstitute(UUID eventId, Instant processedAt) {

    return new ProcessedEvent(eventId, processedAt);
  }

  @Override
  public boolean equals(Object o) {

    if (this == o) {
      return true;
    }

    if (!(o instanceof ProcessedEvent that)) {
      return false;
    }

    return eventId.equals(that.eventId);
  }

  @Override
  public int hashCode() {
    return eventId.hashCode();
  }
}
