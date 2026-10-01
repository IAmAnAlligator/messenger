package com.jeannimi.messenger.domain.message;

import com.jeannimi.messenger.domain.common.DomainId;
import java.util.Objects;
import java.util.UUID;

public record MessageId(UUID value) implements DomainId {

  public MessageId {
    Objects.requireNonNull(value, "Message id must not be null");
  }
}
