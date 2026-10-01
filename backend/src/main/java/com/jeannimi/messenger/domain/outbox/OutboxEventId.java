package com.jeannimi.messenger.domain.outbox;

import com.jeannimi.messenger.domain.common.DomainId;
import java.util.Objects;
import java.util.UUID;

public record OutboxEventId(UUID value) implements DomainId {

  public OutboxEventId {
    Objects.requireNonNull(value, "OutboxEvent id must not be null");
  }

}
