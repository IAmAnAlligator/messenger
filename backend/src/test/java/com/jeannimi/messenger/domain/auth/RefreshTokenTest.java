package com.jeannimi.messenger.domain.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatIllegalStateException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import com.jeannimi.messenger.domain.user.UserId;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RefreshTokenTest {

  private static final UUID TOKEN_UUID = UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789012");

  private static final UUID ANOTHER_TOKEN_UUID =
      UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789013");

  private static final UUID SESSION_UUID = UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789014");

  private static final UUID USER_UUID = UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789015");

  private static final UUID FIRST_REPLACEMENT_UUID =
      UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789016");

  private static final UUID SECOND_REPLACEMENT_UUID =
      UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789017");

  private static final Instant CREATED_AT = Instant.parse("2026-10-15T10:00:00Z");

  private static final Instant EXPIRES_AT = Instant.parse("2026-11-14T10:00:00Z");

  private static final Instant NOW = Instant.parse("2026-10-15T12:00:00Z");

  private RefreshTokenId tokenId;
  private SessionId sessionId;
  private UserId userId;
  private String tokenHash;

  @BeforeEach
  void setUp() {
    tokenId = new RefreshTokenId(TOKEN_UUID);
    sessionId = new SessionId(SESSION_UUID);
    userId = new UserId(USER_UUID);
    tokenHash = "a".repeat(64);
  }

  private RefreshToken createToken() {
    return RefreshToken.create(tokenId, sessionId, userId, tokenHash, CREATED_AT, EXPIRES_AT);
  }

  @Test
  void create_shouldCreateToken_whenArgumentsAreValid() {
    RefreshToken token = createToken();

    assertThat(token.getId()).isEqualTo(tokenId);
    assertThat(token.getSessionId()).isEqualTo(sessionId);
    assertThat(token.getUserId()).isEqualTo(userId);
    assertThat(token.getTokenHash()).isEqualTo(tokenHash);
    assertThat(token.getCreatedAt()).isEqualTo(CREATED_AT);
    assertThat(token.getExpiresAt()).isEqualTo(EXPIRES_AT);
    assertThat(token.getRevokedAt()).isNull();
    assertThat(token.getReplacedByTokenId()).isNull();
  }

  @Test
  void create_shouldThrowNullPointerException_whenIdIsNull() {
    assertThatNullPointerException()
        .isThrownBy(
            () -> RefreshToken.create(null, sessionId, userId, tokenHash, CREATED_AT, EXPIRES_AT))
        .withMessage("id");
  }

  @Test
  void create_shouldThrowNullPointerException_whenSessionIdIsNull() {
    assertThatNullPointerException()
        .isThrownBy(
            () -> RefreshToken.create(tokenId, null, userId, tokenHash, CREATED_AT, EXPIRES_AT))
        .withMessage("sessionId");
  }

  @Test
  void create_shouldThrowNullPointerException_whenUserIdIsNull() {
    assertThatNullPointerException()
        .isThrownBy(
            () -> RefreshToken.create(tokenId, sessionId, null, tokenHash, CREATED_AT, EXPIRES_AT))
        .withMessage("userId");
  }

  @Test
  void create_shouldThrowNullPointerException_whenTokenHashIsNull() {
    assertThatNullPointerException()
        .isThrownBy(
            () -> RefreshToken.create(tokenId, sessionId, userId, null, CREATED_AT, EXPIRES_AT))
        .withMessage("tokenHash");
  }

  @Test
  void create_shouldThrowNullPointerException_whenCreatedAtIsNull() {
    assertThatNullPointerException()
        .isThrownBy(
            () -> RefreshToken.create(tokenId, sessionId, userId, tokenHash, null, EXPIRES_AT))
        .withMessage("createdAt");
  }

  @Test
  void create_shouldThrowNullPointerException_whenExpiresAtIsNull() {
    assertThatNullPointerException()
        .isThrownBy(
            () -> RefreshToken.create(tokenId, sessionId, userId, tokenHash, CREATED_AT, null))
        .withMessage("expiresAt");
  }

  @Test
  void create_shouldThrowIllegalArgumentException_whenTokenHashIsBlank() {
    assertThatIllegalArgumentException()
        .isThrownBy(
            () -> RefreshToken.create(tokenId, sessionId, userId, "   ", CREATED_AT, EXPIRES_AT))
        .withMessage("Token hash must not be blank");
  }

  @Test
  void create_shouldThrowIllegalArgumentException_whenExpirationEqualsCreation() {
    assertThatIllegalArgumentException()
        .isThrownBy(
            () ->
                RefreshToken.create(tokenId, sessionId, userId, tokenHash, CREATED_AT, CREATED_AT))
        .withMessage("Expiration must be after creation");
  }

  @Test
  void create_shouldThrowIllegalArgumentException_whenExpirationIsBeforeCreation() {
    assertThatIllegalArgumentException()
        .isThrownBy(
            () ->
                RefreshToken.create(tokenId, sessionId, userId, tokenHash, EXPIRES_AT, CREATED_AT))
        .withMessage("Expiration must be after creation");
  }

  @Test
  void reconstitute_shouldRestoreToken_whenArgumentsAreValid() {
    RefreshTokenId replacementId = new RefreshTokenId(FIRST_REPLACEMENT_UUID);

    RefreshToken token =
        RefreshToken.reconstitute(
            tokenId, sessionId, userId, tokenHash, CREATED_AT, EXPIRES_AT, NOW, replacementId);

    assertThat(token.getId()).isEqualTo(tokenId);
    assertThat(token.getSessionId()).isEqualTo(sessionId);
    assertThat(token.getUserId()).isEqualTo(userId);
    assertThat(token.getTokenHash()).isEqualTo(tokenHash);
    assertThat(token.getCreatedAt()).isEqualTo(CREATED_AT);
    assertThat(token.getExpiresAt()).isEqualTo(EXPIRES_AT);
    assertThat(token.getRevokedAt()).isEqualTo(NOW);
    assertThat(token.getReplacedByTokenId()).isEqualTo(replacementId);
  }

  @Test
  void isExpired_shouldReturnFalse_whenNowIsBeforeExpiration() {
    RefreshToken token = createToken();

    assertThat(token.isExpired(NOW)).isFalse();
  }

  @Test
  void isExpired_shouldReturnTrue_whenNowEqualsExpiration() {
    RefreshToken token = createToken();

    assertThat(token.isExpired(EXPIRES_AT)).isTrue();
  }

  @Test
  void isExpired_shouldReturnTrue_whenNowIsAfterExpiration() {
    RefreshToken token = createToken();
    Instant now = EXPIRES_AT.plusSeconds(1);

    assertThat(token.isExpired(now)).isTrue();
  }

  @Test
  void isExpired_shouldThrowNullPointerException_whenNowIsNull() {
    RefreshToken token = createToken();

    assertThatNullPointerException().isThrownBy(() -> token.isExpired(null)).withMessage("now");
  }

  @Test
  void isRevoked_shouldReturnFalse_whenTokenHasNotBeenRevoked() {
    RefreshToken token = createToken();

    assertThat(token.isRevoked()).isFalse();
  }

  @Test
  void isRevoked_shouldReturnTrue_whenTokenHasBeenRevoked() {
    RefreshToken token = createToken();

    token.revoke(NOW);

    assertThat(token.isRevoked()).isTrue();
  }

  @Test
  void isActive_shouldReturnTrue_whenTokenIsNotRevokedAndNotExpired() {
    RefreshToken token = createToken();

    assertThat(token.isActive(NOW)).isTrue();
  }

  @Test
  void isActive_shouldReturnFalse_whenTokenIsRevoked() {
    RefreshToken token = createToken();
    token.revoke(NOW);

    assertThat(token.isActive(NOW)).isFalse();
  }

  @Test
  void isActive_shouldReturnFalse_whenTokenIsExpired() {
    RefreshToken token = createToken();

    assertThat(token.isActive(EXPIRES_AT)).isFalse();
  }

  @Test
  void revoke_shouldSetRevokedAt_whenTokenHasNotBeenRevoked() {
    RefreshToken token = createToken();

    token.revoke(NOW);

    assertThat(token.getRevokedAt()).isEqualTo(NOW);
    assertThat(token.isRevoked()).isTrue();
  }

  @Test
  void revoke_shouldNotChangeRevokedAt_whenTokenHasAlreadyBeenRevoked() {
    RefreshToken token = createToken();
    token.revoke(NOW);

    Instant firstRevokedAt = token.getRevokedAt();
    Instant later = NOW.plusSeconds(60);

    token.revoke(later);

    assertThat(token.getRevokedAt()).isEqualTo(firstRevokedAt);
  }

  @Test
  void revoke_shouldThrowNullPointerException_whenNowIsNull() {
    RefreshToken token = createToken();

    assertThatNullPointerException().isThrownBy(() -> token.revoke(null)).withMessage("now");
  }

  @Test
  void replaceWith_shouldRevokeTokenAndSetReplacementId_whenTokenIsActive() {
    RefreshToken token = createToken();
    RefreshTokenId replacementId = new RefreshTokenId(FIRST_REPLACEMENT_UUID);

    token.replaceWith(replacementId, NOW);

    assertThat(token.getRevokedAt()).isEqualTo(NOW);
    assertThat(token.getReplacedByTokenId()).isEqualTo(replacementId);
    assertThat(token.isRevoked()).isTrue();
    assertThat(token.isActive(NOW)).isFalse();
  }

  @Test
  void replaceWith_shouldThrowNullPointerException_whenReplacementIdIsNull() {
    RefreshToken token = createToken();

    assertThatNullPointerException()
        .isThrownBy(() -> token.replaceWith(null, NOW))
        .withMessage("replacementId");
  }

  @Test
  void replaceWith_shouldThrowNullPointerException_whenNowIsNull() {
    RefreshToken token = createToken();
    RefreshTokenId replacementId = new RefreshTokenId(FIRST_REPLACEMENT_UUID);

    assertThatNullPointerException()
        .isThrownBy(() -> token.replaceWith(replacementId, null))
        .withMessage("now");
  }

  @Test
  void replaceWith_shouldThrowIllegalArgumentException_whenReplacingWithSameId() {
    RefreshToken token = createToken();

    assertThatIllegalArgumentException()
        .isThrownBy(() -> token.replaceWith(tokenId, NOW))
        .withMessage("Token cannot replace itself");
  }

  @Test
  void replaceWith_shouldThrowIllegalStateException_whenTokenHasAlreadyBeenReplaced() {
    RefreshToken token = createToken();
    RefreshTokenId firstReplacementId = new RefreshTokenId(FIRST_REPLACEMENT_UUID);
    RefreshTokenId secondReplacementId = new RefreshTokenId(SECOND_REPLACEMENT_UUID);

    token.replaceWith(firstReplacementId, NOW);

    assertThatIllegalStateException()
        .isThrownBy(() -> token.replaceWith(secondReplacementId, NOW))
        .withMessage("Token has already been replaced");
  }

  @Test
  void replaceWith_shouldThrowIllegalStateException_whenTokenIsRevoked() {
    RefreshToken token = createToken();
    token.revoke(NOW);

    RefreshTokenId replacementId = new RefreshTokenId(FIRST_REPLACEMENT_UUID);

    assertThatIllegalStateException()
        .isThrownBy(() -> token.replaceWith(replacementId, NOW))
        .withMessage("Only active tokens can be replaced");
  }

  @Test
  void replaceWith_shouldThrowIllegalStateException_whenTokenIsExpired() {
    RefreshToken token = createToken();
    RefreshTokenId replacementId = new RefreshTokenId(FIRST_REPLACEMENT_UUID);

    assertThatIllegalStateException()
        .isThrownBy(() -> token.replaceWith(replacementId, EXPIRES_AT))
        .withMessage("Only active tokens can be replaced");
  }

  @Test
  void equals_shouldReturnTrue_whenComparingSameInstance() {
    RefreshToken token = createToken();

    assertThat(token).isEqualTo(token);
  }

  @Test
  void equals_shouldReturnTrue_whenIdsAreEqual() {
    RefreshToken first = createToken();

    RefreshToken second =
        RefreshToken.reconstitute(
            tokenId, sessionId, userId, "differentHash", CREATED_AT, EXPIRES_AT, NOW, null);

    assertThat(first).isEqualTo(second);
  }

  @Test
  void equals_shouldReturnFalse_whenIdsAreDifferent() {
    RefreshToken first = createToken();

    RefreshToken second =
        RefreshToken.create(
            new RefreshTokenId(ANOTHER_TOKEN_UUID),
            sessionId,
            userId,
            tokenHash,
            CREATED_AT,
            EXPIRES_AT);

    assertThat(first).isNotEqualTo(second);
  }

  @Test
  void equals_shouldReturnFalse_whenComparedWithDifferentType() {
    RefreshToken token = createToken();

    assertThat(token).isNotEqualTo("token");
  }

  @Test
  void hashCode_shouldBeEqual_whenIdsAreEqual() {
    RefreshToken first = createToken();

    RefreshToken second =
        RefreshToken.reconstitute(
            tokenId, sessionId, userId, "differentHash", CREATED_AT, EXPIRES_AT, null, null);

    assertThat(first.hashCode()).isEqualTo(second.hashCode());
  }

  @Test
  void hashCode_shouldBeDifferent_whenIdsAreDifferent() {
    RefreshToken first = createToken();

    RefreshToken second =
        RefreshToken.create(
            new RefreshTokenId(ANOTHER_TOKEN_UUID),
            sessionId,
            userId,
            tokenHash,
            CREATED_AT,
            EXPIRES_AT);

    assertThat(first.hashCode()).isNotEqualTo(second.hashCode());
  }

  @Test
  void reconstitute_shouldThrowIllegalArgumentException_whenTokenReplacesItself() {
    assertThatIllegalArgumentException()
        .isThrownBy(
            () ->
                RefreshToken.reconstitute(
                    tokenId, sessionId, userId, tokenHash, CREATED_AT, EXPIRES_AT, null, tokenId))
        .withMessage("Token cannot replace itself");
  }
}
