package com.jeannimi.messenger.domain.user;

import com.jeannimi.messenger.domain.common.DomainId;
import java.util.Objects;
import java.util.UUID;

public record UserId(UUID value) implements Comparable<UserId>, DomainId {

  public UserId {
    Objects.requireNonNull(value, "User id must not be null");
  }

  @Override
  public int compareTo(UserId other) {
    Objects.requireNonNull(other, "User id must not be null");

    return value.compareTo(other.value);
  }
}