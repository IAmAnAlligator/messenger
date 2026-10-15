package com.jeannimi.messenger.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;

class UsernameTest {

  @Test
  void constructor_shouldCreateUsername_whenValueIsValid() {
    Username username = new Username("Alex");

    assertThat(username.getValue()).isEqualTo("Alex");
  }

  @Test
  void constructor_shouldThrowIllegalArgumentException_whenValueIsNull() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new Username(null))
        .withMessage("Username must not be null");
  }

  @Test
  void constructor_shouldThrowIllegalArgumentException_whenValueIsEmpty() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new Username(""))
        .withMessage("Username must not be blank");
  }

  @Test
  void constructor_shouldThrowIllegalArgumentException_whenValueContainsOnlyWhitespace() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new Username("   "))
        .withMessage("Username must not be blank");
  }

  @Test
  void constructor_shouldTrimLeadingAndTrailingWhitespace() {
    Username username = new Username("  Alex  ");

    assertThat(username.getValue()).isEqualTo("Alex");
  }

  @Test
  void constructor_shouldAcceptUsername_whenLengthIsMinimum() {
    String value = "A".repeat(Username.MIN_USERNAME_LENGTH);

    Username username = new Username(value);

    assertThat(username.getValue()).isEqualTo(value);
  }

  @Test
  void constructor_shouldThrowIllegalArgumentException_whenValueIsTooShort() {
    String value = "A".repeat(Username.MIN_USERNAME_LENGTH - 1);

    assertThatIllegalArgumentException()
        .isThrownBy(() -> new Username(value))
        .withMessage("Username too short");
  }

  @Test
  void constructor_shouldAcceptUsername_whenLengthIsMaximum() {
    String value = "A".repeat(Username.MAX_USERNAME_LENGTH);

    Username username = new Username(value);

    assertThat(username.getValue()).isEqualTo(value);
  }

  @Test
  void constructor_shouldThrowIllegalArgumentException_whenValueIsTooLong() {
    String value = "A".repeat(Username.MAX_USERNAME_LENGTH + 1);

    assertThatIllegalArgumentException()
        .isThrownBy(() -> new Username(value))
        .withMessage("Username too long");
  }

  @Test
  void constructor_shouldThrowIllegalArgumentException_whenValueContainsNewline() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new Username("Alex\nSmith"))
        .withMessage("Username contains invalid characters");
  }

  @Test
  void constructor_shouldThrowIllegalArgumentException_whenValueContainsTab() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new Username("Alex\tSmith"))
        .withMessage("Username contains invalid characters");
  }

  @Test
  void constructor_shouldThrowIllegalArgumentException_whenValueContainsNullCharacter() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new Username("Alex\u0000Smith"))
        .withMessage("Username contains invalid characters");
  }

  @Test
  void constructor_shouldAcceptUsernameContainingInternalSpaces() {
    Username username = new Username("Alex Smith");

    assertThat(username.getValue()).isEqualTo("Alex Smith");
  }

  @Test
  void constructor_shouldAcceptUsernameContainingUnicodeCharacters() {
    Username username = new Username("Алексей");

    assertThat(username.getValue()).isEqualTo("Алексей");
  }

  @Test
  void equals_shouldReturnTrue_whenComparingSameInstance() {
    Username username = new Username("Alex");

    assertThat(username).isEqualTo(username);
  }

  @Test
  void equals_shouldReturnTrue_whenValuesAreEqual() {
    Username first = new Username("Alex");
    Username second = new Username("Alex");

    assertThat(first).isEqualTo(second);
  }

  @Test
  void equals_shouldReturnTrue_whenValuesAreEqualAfterTrimming() {
    Username first = new Username("Alex");
    Username second = new Username("  Alex  ");

    assertThat(first).isEqualTo(second);
  }

  @Test
  void equals_shouldReturnFalse_whenValuesAreDifferent() {
    Username first = new Username("Alex");
    Username second = new Username("Alexander");

    assertThat(first).isNotEqualTo(second);
  }

  @Test
  void equals_shouldReturnFalse_whenComparedWithDifferentType() {
    Username username = new Username("Alex");

    assertThat(username).isNotEqualTo("Alex");
  }

  @Test
  void hashCode_shouldBeEqual_whenValuesAreEqual() {
    Username first = new Username("Alex");
    Username second = new Username("Alex");

    assertThat(first.hashCode()).isEqualTo(second.hashCode());
  }

  @Test
  void hashCode_shouldBeEqual_whenValuesAreEqualAfterTrimming() {
    Username first = new Username("Alex");
    Username second = new Username("  Alex  ");

    assertThat(first.hashCode()).isEqualTo(second.hashCode());
  }

  @Test
  void hashCode_shouldBeConsistent_whenCalledMultipleTimes() {
    Username username = new Username("Alex");

    int firstHashCode = username.hashCode();
    int secondHashCode = username.hashCode();

    assertThat(secondHashCode).isEqualTo(firstHashCode);
  }

  @Test
  void toString_shouldReturnUsernameValue() {
    Username username = new Username("Alex");

    assertThat(username.toString()).isEqualTo("Alex");
  }

  @Test
  void usernameLengthConstants_shouldHaveExpectedValues() {
    assertThat(Username.MIN_USERNAME_LENGTH).isEqualTo(3);
    assertThat(Username.MAX_USERNAME_LENGTH).isEqualTo(100);
  }
}
