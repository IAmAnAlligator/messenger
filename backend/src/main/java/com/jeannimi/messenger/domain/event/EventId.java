package com.jeannimi.messenger.domain.event;

import com.jeannimi.messenger.domain.common.DomainId;
import java.util.Objects;
import java.util.UUID;

public record EventId(UUID value) implements DomainId {

  public EventId {
    Objects.requireNonNull(value, "Event id must not be null");
  }
}
