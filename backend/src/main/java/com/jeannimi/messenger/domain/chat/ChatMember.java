package com.jeannimi.messenger.domain.chat;

import com.jeannimi.messenger.domain.user.User;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;

@Getter
public final class ChatMember {

  private final Long id;
  private final User user;
  private final ChatRole role;
  private final Instant joinedAt;
  private Long lastReadMessageId;

  private ChatMember(
      Long id,
      User user,
      ChatRole role,
      Instant joinedAt,
      Long lastReadMessageId) {

    this.id = id;
    this.user = Objects.requireNonNull(user, "user");
    this.role = Objects.requireNonNull(role, "role");
    this.joinedAt = Objects.requireNonNull(joinedAt, "joinedAt");
    this.lastReadMessageId = lastReadMessageId;
  }

  public static ChatMember create(
      User user,
      ChatRole role) {

    return new ChatMember(
        null,
        user,
        role,
        Instant.now(),
        null);
  }

  public static ChatMember reconstitute(
      Long id,
      User user,
      ChatRole role,
      Instant joinedAt,
      Long lastReadMessageId) {

    return new ChatMember(
        Objects.requireNonNull(id, "id"),
        user,
        role,
        joinedAt,
        lastReadMessageId);
  }

  public Long getUserId() {
    return user.getId();
  }

  public boolean isAdmin() {
    return role == ChatRole.ADMIN;
  }

  public void markAsRead(Long messageId) {
    this.lastReadMessageId =
        Objects.requireNonNull(messageId, "messageId");
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