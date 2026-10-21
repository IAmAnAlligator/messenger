package com.jeannimi.messenger.domain.notification;

import com.jeannimi.messenger.domain.common.DomainId;
import java.util.Objects;
import java.util.UUID;

public record WebPushSubscriptionId(UUID value) implements DomainId {

  public WebPushSubscriptionId {
    Objects.requireNonNull(value, "Web push subscription id must not be null");
  }
}
