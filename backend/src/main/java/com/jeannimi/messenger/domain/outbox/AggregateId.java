package com.jeannimi.messenger.domain.outbox;

import com.jeannimi.messenger.domain.common.DomainId;
import java.util.Objects;
import java.util.UUID;

public record AggregateId(UUID value) implements DomainId {

  public AggregateId {
    Objects.requireNonNull(value, "AggregateId id must not be null");
  }

}
