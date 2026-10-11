package com.jeannimi.messenger.domain.auth;

import com.jeannimi.messenger.domain.common.DomainId;
import java.util.Objects;
import java.util.UUID;

public record SessionId(UUID value) implements DomainId {

  public SessionId {
    Objects.requireNonNull(value, "Session id must not be null");
  }
}
