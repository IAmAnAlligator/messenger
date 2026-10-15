package com.jeannimi.messenger.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;

class EmailTest {

  @Test
  void constructor_shouldCreateEmail_whenValueIsValid() {
    Email email = new Email("alex@example.com");

    assertThat(email.getValue()).isEqualTo("alex@example.com");
  }

  @Test
  void constructor_shouldTrimWhitespace() {
    Email email = new Email("  alex@example.com  ");

    assertThat(email.getValue()).isEqualTo("alex@example.com");
  }

  @Test
  void constructor_shouldConvertValueToLowerCase() {
    Email email = new Email("Alex@Example.COM");

    assertThat(email.getValue()).isEqualTo("alex@example.com");
  }

  @Test
  void constructor_shouldTrimWhitespaceAndConvertToLowerCase() {
    Email email = new Email("  Alex@Example.COM  ");

    assertThat(email.getValue()).isEqualTo("alex@example.com");
  }

  @Test
  void constructor_shouldThrowIllegalArgumentException_whenValueIsNull() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new Email(null))
        .withMessage("Email must not be null");
  }

  @Test
  void constructor_shouldThrowIllegalArgumentException_whenValueIsEmpty() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new Email(""))
        .withMessage("Email must not be blank");
  }

  @Test
  void constructor_shouldThrowIllegalArgumentException_whenValueContainsOnlyWhitespace() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new Email("   "))
        .withMessage("Email must not be blank");
  }

  @Test
  void constructor_shouldThrowIllegalArgumentException_whenValueExceedsMaximumLength() {
    String localPart = "a".repeat(244);
    String email = localPart + "@example.com";

    assertThat(email).hasSize(255 + 1);

    assertThatIllegalArgumentException()
        .isThrownBy(() -> new Email(email))
        .withMessage("Email must not exceed 255 characters");
  }

  @Test
  void constructor_shouldAcceptEmail_whenValueHasMaximumLength() {
    String localPart = "a".repeat(243);
    String emailValue = localPart + "@example.com";

    assertThat(emailValue).hasSize(255);

    Email email = new Email(emailValue);

    assertThat(email.getValue()).isEqualTo(emailValue);
  }

  @Test
  void constructor_shouldThrowIllegalArgumentException_whenEmailHasNoAtSign() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new Email("alexexample.com"))
        .withMessage("Invalid email");
  }

  @Test
  void constructor_shouldThrowIllegalArgumentException_whenEmailHasNoLocalPart() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new Email("@example.com"))
        .withMessage("Invalid email");
  }

  @Test
  void constructor_shouldThrowIllegalArgumentException_whenEmailHasNoDomain() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new Email("alex@"))
        .withMessage("Invalid email");
  }

  @Test
  void constructor_shouldThrowIllegalArgumentException_whenEmailHasNoDotInDomain() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new Email("alex@example"))
        .withMessage("Invalid email");
  }

  @Test
  void constructor_shouldThrowIllegalArgumentException_whenEmailContainsWhitespace() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new Email("alex smith@example.com"))
        .withMessage("Invalid email");
  }

  @Test
  void constructor_shouldThrowIllegalArgumentException_whenEmailContainsMultipleAtSigns() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new Email("alex@@example.com"))
        .withMessage("Invalid email");
  }

  @Test
  void constructor_shouldThrowIllegalArgumentException_whenEmailHasEmptyDomainLabel() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new Email("alex@example..com"))
        .withMessage("Invalid email");
  }

  @Test
  void equals_shouldReturnTrue_whenComparingSameInstance() {
    Email email = new Email("alex@example.com");

    assertThat(email).isEqualTo(email);
  }

  @Test
  void equals_shouldReturnTrue_whenValuesAreEqual() {
    Email first = new Email("alex@example.com");
    Email second = new Email("alex@example.com");

    assertThat(first).isEqualTo(second);
  }

  @Test
  void equals_shouldReturnTrue_whenValuesDifferOnlyByCase() {
    Email first = new Email("Alex@Example.COM");
    Email second = new Email("alex@example.com");

    assertThat(first).isEqualTo(second);
  }

  @Test
  void equals_shouldReturnTrue_whenValuesDifferOnlyBySurroundingWhitespace() {
    Email first = new Email("  alex@example.com  ");
    Email second = new Email("alex@example.com");

    assertThat(first).isEqualTo(second);
  }

  @Test
  void equals_shouldReturnFalse_whenValuesAreDifferent() {
    Email first = new Email("alex@example.com");
    Email second = new Email("bob@example.com");

    assertThat(first).isNotEqualTo(second);
  }

  @Test
  void hashCode_shouldBeEqual_whenValuesAreEqual() {
    Email first = new Email("Alex@Example.COM");
    Email second = new Email("alex@example.com");

    assertThat(first.hashCode()).isEqualTo(second.hashCode());
  }

  @Test
  void equals_shouldReturnFalse_whenComparedWithDifferentType() {
    Email email = new Email("alex@example.com");

    assertThat(email).isNotEqualTo("alex@example.com");
  }

  @Test
  void hashCode_shouldBeConsistent_whenCalledMultipleTimes() {
    Email email = new Email("alex@example.com");

    int firstHashCode = email.hashCode();
    int secondHashCode = email.hashCode();

    assertThat(secondHashCode).isEqualTo(firstHashCode);
  }

  @Test
  void toString_shouldReturnEmailValue() {
    Email email = new Email("alex@example.com");

    assertThat(email.toString()).isEqualTo("alex@example.com");
  }

  @Test
  void maxEmailLength_shouldBe255() {
    assertThat(Email.MAX_EMAIL_LENGTH).isEqualTo(255);
  }
}
