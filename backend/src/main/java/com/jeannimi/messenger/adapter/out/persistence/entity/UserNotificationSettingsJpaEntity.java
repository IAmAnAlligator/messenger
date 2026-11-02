package com.jeannimi.messenger.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "user_notification_settings")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserNotificationSettingsJpaEntity {

  @Id
  @Column(name = "user_id", nullable = false, updatable = false)
  private UUID userId;

  @Column(name = "web_push_private_chats_enabled", nullable = false)
  private boolean webPushPrivateChatsEnabled;

  @Column(name = "web_push_group_chats_enabled", nullable = false)
  private boolean webPushGroupChatsEnabled;

  @Column(name = "email_private_chats_enabled", nullable = false)
  private boolean emailPrivateChatsEnabled;

  @Column(name = "email_group_chats_enabled", nullable = false)
  private boolean emailGroupChatsEnabled;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  public UserNotificationSettingsJpaEntity(
      UUID userId,
      boolean webPushPrivateChatsEnabled,
      boolean webPushGroupChatsEnabled,
      boolean emailPrivateChatsEnabled,
      boolean emailGroupChatsEnabled,
      Instant createdAt,
      Instant updatedAt) {

    this.userId = userId;

    this.webPushPrivateChatsEnabled = webPushPrivateChatsEnabled;
    this.webPushGroupChatsEnabled = webPushGroupChatsEnabled;

    this.emailPrivateChatsEnabled = emailPrivateChatsEnabled;
    this.emailGroupChatsEnabled = emailGroupChatsEnabled;

    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }
}