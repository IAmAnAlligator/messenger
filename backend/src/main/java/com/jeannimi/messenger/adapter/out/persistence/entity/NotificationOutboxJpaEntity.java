package com.jeannimi.messenger.adapter.out.persistence.entity;

import com.jeannimi.messenger.domain.notification.NotificationChannel;
import com.jeannimi.messenger.domain.notification.NotificationOutboxStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "notification_outbox")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationOutboxJpaEntity {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "event_id", nullable = false, updatable = false)
  private UUID eventId;

  @Column(name = "message_id", nullable = false, updatable = false)
  private UUID messageId;

  @Column(name = "chat_id", nullable = false, updatable = false)
  private UUID chatId;

  @Column(name = "recipient_user_id", nullable = false, updatable = false)
  private UUID recipientUserId;

  @Column(name = "sender_user_id", nullable = false, updatable = false)
  private UUID senderUserId;

  @Column(name = "sender_username", nullable = false, updatable = false, length = 255)
  private String senderUsername;

  @Enumerated(EnumType.STRING)
  @Column(name = "channel", nullable = false, length = 20)
  private NotificationChannel channel;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  private NotificationOutboxStatus status;

  @Column(name = "attempts", nullable = false)
  private int attempts;

  @Column(name = "next_attempt_at", nullable = false)
  private Instant nextAttemptAt;

  @Column(name = "locked_until")
  private Instant lockedUntil;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "sent_at")
  private Instant sentAt;

  @Column(name = "last_error")
  private String lastError;

  public NotificationOutboxJpaEntity(
      UUID id,
      UUID eventId,
      UUID messageId,
      UUID chatId,
      UUID recipientUserId,
      UUID senderUserId,
      String senderUsername,
      NotificationChannel channel,
      Instant createdAt
  ) {
    this.id = id;
    this.eventId = eventId;
    this.messageId = messageId;
    this.chatId = chatId;
    this.recipientUserId = recipientUserId;
    this.senderUserId = senderUserId;
    this.senderUsername = senderUsername;
    this.channel = channel;
    this.status = NotificationOutboxStatus.NEW;
    this.attempts = 0;
    this.nextAttemptAt = createdAt;
    this.createdAt = createdAt;
  }
}