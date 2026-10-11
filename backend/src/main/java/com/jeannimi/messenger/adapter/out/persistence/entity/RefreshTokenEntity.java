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
@Table(name = "refresh_tokens")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshTokenEntity {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Column(name = "session_id", nullable = false, updatable = false)
  private UUID sessionId;

  @Column(name = "user_id", nullable = false, updatable = false)
  private UUID userId;

  @Column(name = "token_hash", nullable = false, unique = true, length = 64, updatable = false)
  private String tokenHash;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Column(name = "expires_at", nullable = false, updatable = false)
  private Instant expiresAt;

  @Column(name = "revoked_at")
  private Instant revokedAt;

  @Column(name = "replaced_by_token_id")
  private UUID replacedByTokenId;

  public RefreshTokenEntity(
      UUID id,
      UUID sessionId,
      UUID userId,
      String tokenHash,
      Instant createdAt,
      Instant expiresAt,
      Instant revokedAt,
      UUID replacedByTokenId) {
    this.id = id;
    this.sessionId = sessionId;
    this.userId = userId;
    this.tokenHash = tokenHash;
    this.createdAt = createdAt;
    this.expiresAt = expiresAt;
    this.revokedAt = revokedAt;
    this.replacedByTokenId = replacedByTokenId;
  }

  public void updateState(Instant revokedAt, UUID replacedByTokenId) {
    this.revokedAt = revokedAt;
    this.replacedByTokenId = replacedByTokenId;
  }
}
