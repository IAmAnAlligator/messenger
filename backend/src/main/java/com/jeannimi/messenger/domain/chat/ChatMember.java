package com.jeannimi.messenger.domain.chat;

import java.time.Instant;
import java.util.Objects;
import lombok.Getter;

@Getter
public final class ChatMember {

  private final Long id;
  private final Long userId;
  private final ChatRole role;
  private final Instant joinedAt;
  private Long lastReadMessageId;

  private ChatMember(
      Long id, Long userId, ChatRole role, Instant joinedAt, Long lastReadMessageId) {

    this.id = id;
    this.userId = Objects.requireNonNull(userId, "userId");
    this.role = Objects.requireNonNull(role, "role");
    this.joinedAt = Objects.requireNonNull(joinedAt, "joinedAt");
    this.lastReadMessageId = lastReadMessageId;
  }

  public static ChatMember create(Long userId, ChatRole role) {

    return new ChatMember(null, userId, role, Instant.now(), null);
  }

  public static ChatMember reconstitute(
      Long id, Long userId, ChatRole role, Instant joinedAt, Long lastReadMessageId) {

    return new ChatMember(
        Objects.requireNonNull(id, "id"), userId, role, joinedAt, lastReadMessageId);
  }

  public boolean isAdmin() {
    return role == ChatRole.ADMIN;
  }

  @Override
  public boolean equals(Object o) {

    if (this == o) {
      return true;
    }

    if (!(o instanceof ChatMember that)) {
      return false;
    }

    return id != null && id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
