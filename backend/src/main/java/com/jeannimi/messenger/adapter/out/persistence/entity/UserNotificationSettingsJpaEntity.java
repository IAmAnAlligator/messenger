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

  @Column(name = "web_push_enabled", nullable = false)
  private boolean webPushEnabled;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  public UserNotificationSettingsJpaEntity(
      UUID userId, boolean webPushEnabled, Instant createdAt, Instant updatedAt) {

    this.userId = userId;
    this.webPushEnabled = webPushEnabled;
    this.createdAt = createdAt;
    this.updatedAt = updatedAt;
  }
}
