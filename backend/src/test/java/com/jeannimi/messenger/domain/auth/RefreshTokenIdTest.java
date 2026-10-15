package com.jeannimi.messenger.domain.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class RefreshTokenIdTest {

  @Test
  void constructor_shouldCreateRefreshTokenId_whenValueIsValid() {
    UUID value = UUID.randomUUID();

    RefreshTokenId refreshTokenId = new RefreshTokenId(value);

    assertThat(refreshTokenId.value()).isEqualTo(value);
  }

  @Test
  void constructor_shouldThrowNullPointerException_whenValueIsNull() {
    assertThatNullPointerException()
        .isThrownBy(() -> new RefreshTokenId(null))
        .withMessage("Refresh token id must not be null");
  }

  @Test
  void equals_shouldReturnTrue_whenValuesAreEqual() {
    UUID value = UUID.randomUUID();

    RefreshTokenId first = new RefreshTokenId(value);
    RefreshTokenId second = new RefreshTokenId(value);

    assertThat(first).isEqualTo(second);
  }

  @Test
  void equals_shouldReturnFalse_whenValuesAreDifferent() {
    RefreshTokenId first = new RefreshTokenId(UUID.randomUUID());
    RefreshTokenId second = new RefreshTokenId(UUID.randomUUID());

    assertThat(first).isNotEqualTo(second);
  }

  @Test
  void hashCode_shouldBeEqual_whenValuesAreEqual() {
    UUID value = UUID.randomUUID();

    RefreshTokenId first = new RefreshTokenId(value);
    RefreshTokenId second = new RefreshTokenId(value);

    assertThat(first.hashCode()).isEqualTo(second.hashCode());
  }
}
