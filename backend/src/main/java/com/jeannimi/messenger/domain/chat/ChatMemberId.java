package com.jeannimi.messenger.domain.chat;

import com.jeannimi.messenger.domain.common.DomainId;
import java.util.Objects;
import java.util.UUID;

public record ChatMemberId(UUID value) implements DomainId {

  public ChatMemberId {
    Objects.requireNonNull(value, "Chat member id must not be null");
  }
}
