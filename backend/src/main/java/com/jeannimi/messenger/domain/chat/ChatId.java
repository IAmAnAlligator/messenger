package com.jeannimi.messenger.domain.chat;

import com.jeannimi.messenger.domain.common.DomainId;
import java.util.Objects;
import java.util.UUID;

public record ChatId(UUID value) implements DomainId {

  public ChatId {
    Objects.requireNonNull(value, "Chat id must not be null");
  }
}
