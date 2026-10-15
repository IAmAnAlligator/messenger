package com.jeannimi.messenger.domain.auth;

import com.jeannimi.messenger.domain.user.UserId;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;

@Getter
public final class RefreshToken {

  private final RefreshTokenId id;

  private final SessionId sessionId;

  private final UserId userId;

  private final String tokenHash;

  private final Instant createdAt;

  private final Instant expiresAt;

  private Instant revokedAt;

  private RefreshTokenId replacedByTokenId;

  private RefreshToken(
      RefreshTokenId id,
      SessionId sessionId,
      UserId userId,
      String tokenHash,
      Instant createdAt,
      Instant expiresAt,
      Instant revokedAt,
      RefreshTokenId replacedByTokenId) {

    this.id = Objects.requireNonNull(id, "id");
    this.sessionId = Objects.requireNonNull(sessionId, "sessionId");
    this.userId = Objects.requireNonNull(userId, "userId");
    this.tokenHash = Objects.requireNonNull(tokenHash, "tokenHash");
    this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    this.expiresAt = Objects.requireNonNull(expiresAt, "expiresAt");

    if (id.equals(replacedByTokenId)) {
      throw new IllegalArgumentException("Token cannot replace itself");
    }

    if (tokenHash.isBlank()) {
      throw new IllegalArgumentException("Token hash must not be blank");
    }

    if (!expiresAt.isAfter(createdAt)) {
      throw new IllegalArgumentException("Expiration must be after creation");
    }

    this.revokedAt = revokedAt;
    this.replacedByTokenId = replacedByTokenId;
  }

  public static RefreshToken create(
      RefreshTokenId id,
      SessionId sessionId,
      UserId userId,
      String tokenHash,
      Instant createdAt,
      Instant expiresAt) {

    return new RefreshToken(id, sessionId, userId, tokenHash, createdAt, expiresAt, null, null);
  }

  public static RefreshToken reconstitute(
      RefreshTokenId id,
      SessionId sessionId,
      UserId userId,
      String tokenHash,
      Instant createdAt,
      Instant expiresAt,
      Instant revokedAt,
      RefreshTokenId replacedByTokenId) {

    return new RefreshToken(
        id, sessionId, userId, tokenHash, createdAt, expiresAt, revokedAt, replacedByTokenId);
  }

  public boolean isExpired(Instant now) {
    Objects.requireNonNull(now, "now");

    return !expiresAt.isAfter(now);
  }

  public boolean isRevoked() {
    return revokedAt != null;
  }

  public boolean isActive(Instant now) {
    return !isRevoked() && !isExpired(now);
  }

  public void revoke(Instant now) {
    Objects.requireNonNull(now, "now");

    if (isRevoked()) {
      return;
    }

    revokedAt = now;
  }

  public void replaceWith(RefreshTokenId replacementId, Instant now) {

    Objects.requireNonNull(replacementId, "replacementId");
    Objects.requireNonNull(now, "now");

    if (replacementId.equals(id)) {
      throw new IllegalArgumentException("Token cannot replace itself");
    }

    if (replacedByTokenId != null) {
      throw new IllegalStateException("Token has already been replaced");
    }

    if (!isActive(now)) {
      throw new IllegalStateException("Only active tokens can be replaced");
    }

    revokedAt = now;
    replacedByTokenId = replacementId;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof RefreshToken that)) {
      return false;
    }

    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
