package com.jeannimi.messenger.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserIdTest {

  @Test
  void constructor_shouldCreateUserId_whenValueIsValid() {
    UUID value = UUID.randomUUID();

    UserId userId = new UserId(value);

    assertThat(userId.value()).isEqualTo(value);
  }

  @Test
  void constructor_shouldThrowNullPointerException_whenValueIsNull() {
    assertThatNullPointerException()
        .isThrownBy(() -> new UserId(null))
        .withMessage("User id must not be null");
  }

  @Test
  void equals_shouldReturnTrue_whenValuesAreEqual() {
    UUID value = UUID.randomUUID();

    UserId first = new UserId(value);
    UserId second = new UserId(value);

    assertThat(first).isEqualTo(second);
  }

  @Test
  void equals_shouldReturnFalse_whenValuesAreDifferent() {
    UserId first = new UserId(UUID.randomUUID());
    UserId second = new UserId(UUID.randomUUID());

    assertThat(first).isNotEqualTo(second);
  }

  @Test
  void hashCode_shouldBeEqual_whenValuesAreEqual() {
    UUID value = UUID.randomUUID();

    UserId first = new UserId(value);
    UserId second = new UserId(value);

    assertThat(first.hashCode()).isEqualTo(second.hashCode());
  }

  @Test
  void compareTo_shouldReturnZero_whenValuesAreEqual() {
    UUID value = UUID.randomUUID();

    UserId first = new UserId(value);
    UserId second = new UserId(value);

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
    UserId userId = new UserId(UUID.randomUUID());

    assertThatNullPointerException()
        .isThrownBy(() -> userId.compareTo(null))
        .withMessage("User id must not be null");
  }
}
