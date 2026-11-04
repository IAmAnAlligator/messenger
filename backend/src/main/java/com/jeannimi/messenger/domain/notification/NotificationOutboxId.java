package com.jeannimi.messenger.domain.notification;

import java.util.Objects;
import java.util.UUID;

public record NotificationOutboxId(UUID value) {

  public NotificationOutboxId {
    Objects.requireNonNull(value, "value");
  }
}
