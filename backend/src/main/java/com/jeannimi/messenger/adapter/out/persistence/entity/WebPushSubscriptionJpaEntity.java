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
@Table(name = "web_push_subscriptions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class WebPushSubscriptionJpaEntity {

  @Id private UUID id;

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  @Column(name = "endpoint", nullable = false, columnDefinition = "TEXT")
  private String endpoint;

  @Column(name = "p256dh", nullable = false, length = 255)
  private String p256dh;

  @Column(name = "auth", nullable = false, length = 255)
  private String auth;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "last_used_at")
  private Instant lastUsedAt;

  public WebPushSubscriptionJpaEntity(
      UUID id,
      UUID userId,
      String endpoint,
      String p256dh,
      String auth,
      Instant createdAt,
      Instant lastUsedAt) {

    this.id = id;
    this.userId = userId;
    this.endpoint = endpoint;
    this.p256dh = p256dh;
    this.auth = auth;
    this.createdAt = createdAt;
    this.lastUsedAt = lastUsedAt;
  }
}
