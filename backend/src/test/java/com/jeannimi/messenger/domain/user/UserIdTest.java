package com.jeannimi.messenger.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserIdTest {

  private static final UUID USER_UUID = UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789012");

  private static final UUID ANOTHER_USER_UUID =
      UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789013");

  @Test
  void constructor_shouldCreateUserId_whenValueIsValid() {
    UserId userId = new UserId(USER_UUID);

    assertThat(userId.value()).isEqualTo(USER_UUID);
  }

  @Test
  void constructor_shouldThrowNullPointerException_whenValueIsNull() {
    assertThatNullPointerException()
        .isThrownBy(() -> new UserId(null))
        .withMessage("User id must not be null");
  }

  @Test
  void equals_shouldReturnTrue_whenValuesAreEqual() {
    UserId first = new UserId(USER_UUID);
    UserId second = new UserId(USER_UUID);

    assertThat(first).isEqualTo(second);
  }

  @Test
  void equals_shouldReturnFalse_whenValuesAreDifferent() {
    UserId first = new UserId(USER_UUID);
    UserId second = new UserId(ANOTHER_USER_UUID);

    assertThat(first).isNotEqualTo(second);
  }

  @Test
  void equals_shouldReturnTrue_whenComparedWithItself() {
    UserId userId = new UserId(USER_UUID);

    assertThat(userId).isEqualTo(userId);
  }

  @Test
  void equals_shouldReturnFalse_whenComparedWithNull() {
    UserId userId = new UserId(USER_UUID);

    assertThat(userId).isNotEqualTo(null);
  }

  @Test
  void equals_shouldReturnFalse_whenComparedWithDifferentType() {
    UserId userId = new UserId(USER_UUID);

    assertThat(userId).isNotEqualTo(USER_UUID);
  }

  @Test
  void hashCode_shouldBeEqual_whenValuesAreEqual() {
    UserId first = new UserId(USER_UUID);
    UserId second = new UserId(USER_UUID);

    assertThat(first.hashCode()).isEqualTo(second.hashCode());
  }

  @Test
  void compareTo_shouldReturnZero_whenValuesAreEqual() {
    UserId first = new UserId(USER_UUID);
    UserId second = new UserId(USER_UUID);

    assertThat(first.compareTo(second)).isZero();
  }

  @Test
  void compareTo_shouldReturnNegativeValue_whenValueIsLess() {
    UserId first = new UserId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    UserId second = new UserId(UUID.fromString("00000000-0000-0000-0000-000000000002"));

    assertThat(first.compareTo(second)).isNegative();
  }

  @Test
  void compareTo_shouldReturnPositiveValue_whenValueIsGreater() {
    UserId first = new UserId(UUID.fromString("00000000-0000-0000-0000-000000000002"));
    UserId second = new UserId(UUID.fromString("00000000-0000-0000-0000-000000000001"));

    assertThat(first.compareTo(second)).isPositive();
  }

  @Test
  void compareTo_shouldThrowNullPointerException_whenOtherIsNull() {
    UserId userId = new UserId(USER_UUID);

    assertThatNullPointerException()
        .isThrownBy(() -> userId.compareTo(null))
        .withMessage("User id must not be null");
  }
}
