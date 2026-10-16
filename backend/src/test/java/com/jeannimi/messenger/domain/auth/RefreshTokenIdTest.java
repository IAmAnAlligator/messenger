package com.jeannimi.messenger.domain.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class RefreshTokenIdTest {

  private static final UUID TOKEN_UUID = UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789012");

  private static final UUID ANOTHER_TOKEN_UUID =
      UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789013");

  @Test
  void constructor_shouldCreateRefreshTokenId_whenValueIsValid() {
    RefreshTokenId refreshTokenId = new RefreshTokenId(TOKEN_UUID);

    assertThat(refreshTokenId.value()).isEqualTo(TOKEN_UUID);
  }

  @Test
  void constructor_shouldThrowNullPointerException_whenValueIsNull() {
    assertThatNullPointerException()
        .isThrownBy(() -> new RefreshTokenId(null))
        .withMessage("Refresh token id must not be null");
  }

  @Test
  void equals_shouldReturnTrue_whenValuesAreEqual() {
    RefreshTokenId first = new RefreshTokenId(TOKEN_UUID);
    RefreshTokenId second = new RefreshTokenId(TOKEN_UUID);

    assertThat(first).isEqualTo(second);
  }

  @Test
  void equals_shouldReturnFalse_whenValuesAreDifferent() {
    RefreshTokenId first = new RefreshTokenId(TOKEN_UUID);
    RefreshTokenId second = new RefreshTokenId(ANOTHER_TOKEN_UUID);

    assertThat(first).isNotEqualTo(second);
  }

  @Test
  void hashCode_shouldBeEqual_whenValuesAreEqual() {
    RefreshTokenId first = new RefreshTokenId(TOKEN_UUID);
    RefreshTokenId second = new RefreshTokenId(TOKEN_UUID);

    assertThat(first.hashCode()).isEqualTo(second.hashCode());
  }
}
