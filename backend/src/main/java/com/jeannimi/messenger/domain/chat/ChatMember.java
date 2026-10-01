package com.jeannimi.messenger.domain.chat;

import com.jeannimi.messenger.domain.message.MessageId;
import com.jeannimi.messenger.domain.user.User;
import com.jeannimi.messenger.domain.user.UserId;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;

@Getter
public final class ChatMember {

  private final ChatMemberId id;
  private final User user;
  private final ChatRole role;
  private final Instant joinedAt;
  private MessageId lastReadMessageId;

  private ChatMember(
      ChatMemberId id, User user, ChatRole role, Instant joinedAt, MessageId lastReadMessageId) {

    this.id = Objects.requireNonNull(id, "id");
    this.user = Objects.requireNonNull(user, "user");
    this.role = Objects.requireNonNull(role, "role");
    this.joinedAt = Objects.requireNonNull(joinedAt, "joinedAt");
    this.lastReadMessageId = lastReadMessageId;
  }

  public static ChatMember create(ChatMemberId id, User user, ChatRole role) {

    return new ChatMember(id, user, role, Instant.now(), null);
  }

  public static ChatMember reconstitute(
      ChatMemberId id, User user, ChatRole role, Instant joinedAt, MessageId lastReadMessageId) {

    return new ChatMember(id, user, role, joinedAt, lastReadMessageId);
  }

  public UserId getUserId() {
    return user.getId();
  }

  public boolean isAdmin() {
    return role == ChatRole.ADMIN;
  }

  public void markAsRead(MessageId messageId) {
    this.lastReadMessageId = Objects.requireNonNull(messageId, "messageId");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof ChatMember that)) {
      return false;
    }

    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
