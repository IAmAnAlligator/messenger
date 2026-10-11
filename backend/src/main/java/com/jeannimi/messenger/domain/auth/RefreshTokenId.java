package com.jeannimi.messenger.domain.auth;

import com.jeannimi.messenger.domain.common.DomainId;
import java.util.Objects;
import java.util.UUID;

public record RefreshTokenId(UUID value) implements DomainId {

  public RefreshTokenId {
    Objects.requireNonNull(value, "Refresh token id must not be null");
  }
}
