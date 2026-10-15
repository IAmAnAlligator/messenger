package com.jeannimi.messenger.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;

class PasswordTest {

  @Test
  void constructor_shouldCreatePassword_whenValueIsValid() {
    Password password = new Password("secret123");

    assertThat(password.getValue()).isEqualTo("secret123");
  }

  @Test
  void constructor_shouldAcceptPassword_whenLengthIsMinimum() {
    String value = "a".repeat(Password.MIN_PASSWORD_LENGTH);

    Password password = new Password(value);

    assertThat(password.getValue()).isEqualTo(value);
  }

  @Test
  void constructor_shouldAcceptPassword_whenLengthIsMaximum() {
    String value = "a".repeat(Password.MAX_PASSWORD_LENGTH);

    Password password = new Password(value);

    assertThat(password.getValue()).isEqualTo(value);
  }

  @Test
  void constructor_shouldThrowIllegalArgumentException_whenValueIsNull() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new Password(null))
        .withMessage("Password must not be null");
  }

  @Test
  void constructor_shouldThrowIllegalArgumentException_whenValueIsEmpty() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new Password(""))
        .withMessage("Password must not be blank");
  }

  @Test
  void constructor_shouldThrowIllegalArgumentException_whenValueContainsOnlyWhitespace() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new Password("      "))
        .withMessage("Password must not be blank");
  }

  @Test
  void constructor_shouldThrowIllegalArgumentException_whenValueIsTooShort() {
    String value = "a".repeat(Password.MIN_PASSWORD_LENGTH - 1);

    assertThatIllegalArgumentException()
        .isThrownBy(() -> new Password(value))
        .withMessage("Password too short");
  }

  @Test
  void constructor_shouldThrowIllegalArgumentException_whenValueIsTooLong() {
    String value = "a".repeat(Password.MAX_PASSWORD_LENGTH + 1);

    assertThatIllegalArgumentException()
        .isThrownBy(() -> new Password(value))
        .withMessage("Password too long");
  }

  @Test
  void constructor_shouldThrowIllegalArgumentException_whenValueContainsNewline() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new Password("abc\n123"))
        .withMessage("Password contains invalid characters");
  }

  @Test
  void constructor_shouldThrowIllegalArgumentException_whenValueContainsTab() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new Password("abc\t123"))
        .withMessage("Password contains invalid characters");
  }

  @Test
  void constructor_shouldThrowIllegalArgumentException_whenValueContainsNullCharacter() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new Password("abc\u0000123"))
        .withMessage("Password contains invalid characters");
  }

  @Test
  void constructor_shouldPreserveLeadingAndTrailingWhitespace() {
    String value = "  secret123  ";

    Password password = new Password(value);

    assertThat(password.getValue()).isEqualTo(value);
  }

  @Test
  void constructor_shouldAcceptPasswordContainingSpaces() {
    String value = "my secret password";

    Password password = new Password(value);

    assertThat(password.getValue()).isEqualTo(value);
  }

  @Test
  void constructor_shouldAcceptPasswordContainingUnicodeCharacters() {
    String value = "пароль123";

    Password password = new Password(value);

    assertThat(password.getValue()).isEqualTo(value);
  }

  @Test
  void equals_shouldReturnTrue_whenComparingSameInstance() {
    Password password = new Password("secret123");

    assertThat(password).isEqualTo(password);
  }

  @Test
  void equals_shouldReturnTrue_whenValuesAreEqual() {
    Password first = new Password("secret123");
    Password second = new Password("secret123");

    assertThat(first).isEqualTo(second);
  }

  @Test
  void equals_shouldReturnFalse_whenValuesAreDifferent() {
    Password first = new Password("secret123");
    Password second = new Password("different123");

    assertThat(first).isNotEqualTo(second);
  }

  @Test
  void equals_shouldReturnFalse_whenComparedWithDifferentType() {
    Password password = new Password("secret123");

    assertThat(password).isNotEqualTo("secret123");
  }

  @Test
  void hashCode_shouldBeEqual_whenValuesAreEqual() {
    Password first = new Password("secret123");
    Password second = new Password("secret123");

    assertThat(first.hashCode()).isEqualTo(second.hashCode());
  }

  @Test
  void hashCode_shouldBeConsistent_whenCalledMultipleTimes() {
    Password password = new Password("secret123");

    int firstHashCode = password.hashCode();
    int secondHashCode = password.hashCode();

    assertThat(secondHashCode).isEqualTo(firstHashCode);
  }

  @Test
  void toString_shouldReturnMaskedValue() {
    Password password = new Password("secret123");

    assertThat(password.toString()).isEqualTo("********");
  }

  @Test
  void passwordLengthConstants_shouldHaveExpectedValues() {
    assertThat(Password.MIN_PASSWORD_LENGTH).isEqualTo(6);
    assertThat(Password.MAX_PASSWORD_LENGTH).isEqualTo(30);
  }
}
