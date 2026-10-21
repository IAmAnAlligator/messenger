package com.jeannimi.messenger.domain.notification;

import com.jeannimi.messenger.domain.event.EventId;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;

@Getter
public final class NotificationEvent {

  private final EventId id;
  private final Instant processedAt;

  private NotificationEvent(EventId id, Instant processedAt) {
    this.id = Objects.requireNonNull(id, "eventId");
    this.processedAt = Objects.requireNonNull(processedAt, "processedAt");
  }

  public static NotificationEvent create(EventId id) {
    return new NotificationEvent(id, Instant.now());
  }

  public static NotificationEvent reconstitute(EventId id, Instant processedAt) {
    return new NotificationEvent(id, processedAt);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof NotificationEvent that)) {
      return false;
    }

    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
