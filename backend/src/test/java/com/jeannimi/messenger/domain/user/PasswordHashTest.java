package com.jeannimi.messenger.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;

class PasswordHashTest {

  @Test
  void constructor_shouldCreatePasswordHash_whenValueIsValid() {
    String value = "$2a$10$abcdefghijklmnopqrstuvwxyz123456789012345678901";

    PasswordHash passwordHash = new PasswordHash(value);

    assertThat(passwordHash.getValue()).isEqualTo(value);
  }

  @Test
  void constructor_shouldThrowIllegalArgumentException_whenValueIsNull() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new PasswordHash(null))
        .withMessage("Password hash must not be null");
  }

  @Test
  void constructor_shouldThrowIllegalArgumentException_whenValueIsEmpty() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new PasswordHash(""))
        .withMessage("Password hash must not be blank");
  }

  @Test
  void constructor_shouldThrowIllegalArgumentException_whenValueContainsOnlyWhitespace() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new PasswordHash("     "))
        .withMessage("Password hash must not be blank");
  }

  @Test
  void constructor_shouldThrowIllegalArgumentException_whenValueContainsNewline() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new PasswordHash("hash\nvalue"))
        .withMessage("Password hash contains invalid characters");
  }

  @Test
  void constructor_shouldThrowIllegalArgumentException_whenValueContainsTab() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new PasswordHash("hash\tvalue"))
        .withMessage("Password hash contains invalid characters");
  }

  @Test
  void constructor_shouldThrowIllegalArgumentException_whenValueContainsNullCharacter() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new PasswordHash("hash\u0000value"))
        .withMessage("Password hash contains invalid characters");
  }

  @Test
  void constructor_shouldPreserveLeadingAndTrailingWhitespace() {
    String value = "  validHash  ";

    PasswordHash passwordHash = new PasswordHash(value);

    assertThat(passwordHash.getValue()).isEqualTo(value);
  }

  @Test
  void constructor_shouldAcceptHashContainingUnicodeCharacters() {
    String value = "хэш123";

    PasswordHash passwordHash = new PasswordHash(value);

    assertThat(passwordHash.getValue()).isEqualTo(value);
  }

  @Test
  void equals_shouldReturnTrue_whenComparingSameInstance() {
    PasswordHash passwordHash = new PasswordHash("hash123");

    assertThat(passwordHash).isEqualTo(passwordHash);
  }

  @Test
  void equals_shouldReturnTrue_whenValuesAreEqual() {
    PasswordHash first = new PasswordHash("hash123");
    PasswordHash second = new PasswordHash("hash123");

    assertThat(first).isEqualTo(second);
  }

  @Test
  void equals_shouldReturnFalse_whenValuesAreDifferent() {
    PasswordHash first = new PasswordHash("hash123");
    PasswordHash second = new PasswordHash("differentHash");

    assertThat(first).isNotEqualTo(second);
  }

  @Test
  void equals_shouldReturnFalse_whenComparedWithDifferentType() {
    PasswordHash passwordHash = new PasswordHash("hash123");

    assertThat(passwordHash).isNotEqualTo("hash123");
  }

  @Test
  void hashCode_shouldBeEqual_whenValuesAreEqual() {
    PasswordHash first = new PasswordHash("hash123");
    PasswordHash second = new PasswordHash("hash123");

    assertThat(first.hashCode()).isEqualTo(second.hashCode());
  }

  @Test
  void hashCode_shouldBeConsistent_whenCalledMultipleTimes() {
    PasswordHash passwordHash = new PasswordHash("hash123");

    int firstHashCode = passwordHash.hashCode();
    int secondHashCode = passwordHash.hashCode();

    assertThat(secondHashCode).isEqualTo(firstHashCode);
  }

  @Test
  void toString_shouldReturnMaskedValue() {
    PasswordHash passwordHash = new PasswordHash("sensitiveHash");

    assertThat(passwordHash.toString()).isEqualTo("********");
  }
}
