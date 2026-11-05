package com.jeannimi.messenger.domain.notification;

import com.jeannimi.messenger.domain.chat.ChatId;
import com.jeannimi.messenger.domain.chat.ChatType;
import com.jeannimi.messenger.domain.common.EventId;
import com.jeannimi.messenger.domain.message.MessageId;
import com.jeannimi.messenger.domain.user.UserId;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;

@Getter
public final class NotificationOutbox {

  private final NotificationOutboxId id;
  private final EventId eventId;
  private final MessageId messageId;
  private final ChatId chatId;
  private final ChatType chatType;
  private final String chatName;
  private final UserId recipientUserId;
  private final UserId senderUserId;
  private final String senderUsername;
  private final NotificationChannel channel;
  private final NotificationOutboxStatus status;
  private final int attempts;
  private final Instant nextAttemptAt;
  private final Instant lockedUntil;
  private final Instant createdAt;
  private final Instant sentAt;
  private final String lastError;

  private NotificationOutbox(
      NotificationOutboxId id,
      EventId eventId,
      MessageId messageId,
      ChatId chatId,
      ChatType chatType,
      String chatName,
      UserId recipientUserId,
      UserId senderUserId,
      String senderUsername,
      NotificationChannel channel,
      NotificationOutboxStatus status,
      int attempts,
      Instant nextAttemptAt,
      Instant lockedUntil,
      Instant createdAt,
      Instant sentAt,
      String lastError) {
    this.id = Objects.requireNonNull(id, "id");
    this.eventId = Objects.requireNonNull(eventId, "eventId");
    this.messageId = Objects.requireNonNull(messageId, "messageId");
    this.chatId = Objects.requireNonNull(chatId, "chatId");
    this.chatType = Objects.requireNonNull(chatType, "chatType");
    this.chatName = chatName;
    this.recipientUserId = Objects.requireNonNull(recipientUserId, "recipientUserId");
    this.senderUserId = Objects.requireNonNull(senderUserId, "senderUserId");
    this.senderUsername = Objects.requireNonNull(senderUsername, "senderUsername");
    this.channel = Objects.requireNonNull(channel, "channel");
    this.status = Objects.requireNonNull(status, "status");
    this.nextAttemptAt = Objects.requireNonNull(nextAttemptAt, "nextAttemptAt");
    this.createdAt = Objects.requireNonNull(createdAt, "createdAt");

    if (attempts < 0) {
      throw new IllegalArgumentException("attempts must not be negative");
    }

    this.attempts = attempts;
    this.lockedUntil = lockedUntil;
    this.sentAt = sentAt;
    this.lastError = lastError;
  }

  public static NotificationOutbox create(
      NotificationOutboxId id,
      EventId eventId,
      MessageId messageId,
      ChatId chatId,
      ChatType chatType,
      String chatName,
      UserId recipientUserId,
      UserId senderUserId,
      String senderUsername,
      NotificationChannel channel,
      Instant now) {
    Objects.requireNonNull(now, "now");

    return new NotificationOutbox(
        id,
        eventId,
        messageId,
        chatId,
        chatType,
        chatName,
        recipientUserId,
        senderUserId,
        senderUsername,
        channel,
        NotificationOutboxStatus.NEW,
        0,
        now,
        null,
        now,
        null,
        null);
  }

  public static NotificationOutbox reconstitute(
      NotificationOutboxId id,
      EventId eventId,
      MessageId messageId,
      ChatId chatId,
      ChatType chatType,
      String chatName,
      UserId recipientUserId,
      UserId senderUserId,
      String senderUsername,
      NotificationChannel channel,
      NotificationOutboxStatus status,
      int attempts,
      Instant nextAttemptAt,
      Instant lockedUntil,
      Instant createdAt,
      Instant sentAt,
      String lastError) {
    return new NotificationOutbox(
        id,
        eventId,
        messageId,
        chatId,
        chatType,
        chatName,
        recipientUserId,
        senderUserId,
        senderUsername,
        channel,
        status,
        attempts,
        nextAttemptAt,
        lockedUntil,
        createdAt,
        sentAt,
        lastError);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof NotificationOutbox that)) {
      return false;
    }

    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
