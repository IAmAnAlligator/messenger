package com.jeannimi.messenger.domain.event;

import java.time.Instant;
import java.util.Objects;
import lombok.Getter;

@Getter
public final class ProcessedEvent {

  private final EventId id;
  private final Instant processedAt;

  private ProcessedEvent(EventId id, Instant processedAt) {

    this.id = Objects.requireNonNull(id, "eventId");
    this.processedAt = Objects.requireNonNull(processedAt, "processedAt");
  }

  public static ProcessedEvent create(EventId id) {

    return new ProcessedEvent(id, Instant.now());
  }

  public static ProcessedEvent reconstitute(EventId id, Instant processedAt) {

    return new ProcessedEvent(id, processedAt);
  }

  @Override
  public boolean equals(Object o) {

    if (this == o) {
      return true;
    }

    if (!(o instanceof ProcessedEvent that)) {
      return false;
    }

    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
