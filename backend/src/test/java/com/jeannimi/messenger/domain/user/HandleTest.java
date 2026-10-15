package com.jeannimi.messenger.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;

import org.junit.jupiter.api.Test;

class HandleTest {

  @Test
  void constructor_shouldCreateHandle_whenValueIsValid() {
    Handle handle = new Handle("abc1234");

    assertThat(handle.getValue()).isEqualTo("abc1234");
  }

  @Test
  void constructor_shouldTrimWhitespace() {
    Handle handle = new Handle("  abc1234  ");

    assertThat(handle.getValue()).isEqualTo("abc1234");
  }

  @Test
  void constructor_shouldConvertValueToLowerCase() {
    Handle handle = new Handle("ABC1234");

    assertThat(handle.getValue()).isEqualTo("abc1234");
  }

  @Test
  void constructor_shouldTrimWhitespaceAndConvertToLowerCase() {
    Handle handle = new Handle("  AbC1234  ");

    assertThat(handle.getValue()).isEqualTo("abc1234");
  }

  @Test
  void constructor_shouldAcceptHandleContainingOnlyLetters() {
    Handle handle = new Handle("abcdefg");

    assertThat(handle.getValue()).isEqualTo("abcdefg");
  }

  @Test
  void constructor_shouldAcceptHandleContainingOnlyDigits() {
    Handle handle = new Handle("1234567");

    assertThat(handle.getValue()).isEqualTo("1234567");
  }

  @Test
  void constructor_shouldThrowIllegalArgumentException_whenValueIsNull() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new Handle(null))
        .withMessage("Handle must not be null");
  }

  @Test
  void constructor_shouldThrowIllegalArgumentException_whenValueIsEmpty() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new Handle(""))
        .withMessage("Handle must contain exactly 7 lowercase letters or digits");
  }

  @Test
  void constructor_shouldThrowIllegalArgumentException_whenValueContainsOnlyWhitespace() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new Handle("   "))
        .withMessage("Handle must contain exactly 7 lowercase letters or digits");
  }

  @Test
  void constructor_shouldThrowIllegalArgumentException_whenValueIsTooShort() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new Handle("abc123"))
        .withMessage("Handle must contain exactly 7 lowercase letters or digits");
  }

  @Test
  void constructor_shouldThrowIllegalArgumentException_whenValueIsTooLong() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new Handle("abc12345"))
        .withMessage("Handle must contain exactly 7 lowercase letters or digits");
  }

  @Test
  void constructor_shouldThrowIllegalArgumentException_whenValueContainsSpecialCharacters() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new Handle("abc_123"))
        .withMessage("Handle must contain exactly 7 lowercase letters or digits");
  }

  @Test
  void constructor_shouldThrowIllegalArgumentException_whenValueContainsAtSign() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new Handle("@abc123"))
        .withMessage("Handle must contain exactly 7 lowercase letters or digits");
  }

  @Test
  void constructor_shouldThrowIllegalArgumentException_whenValueContainsInternalWhitespace() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new Handle("abc 123"))
        .withMessage("Handle must contain exactly 7 lowercase letters or digits");
  }

  @Test
  void constructor_shouldThrowIllegalArgumentException_whenValueContainsNonLatinLetters() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> new Handle("абвг123"))
        .withMessage("Handle must contain exactly 7 lowercase letters or digits");
  }

  @Test
  void asTag_shouldReturnValuePrefixedWithAtSign() {
    Handle handle = new Handle("abc1234");

    assertThat(handle.asTag()).isEqualTo("@abc1234");
  }

  @Test
  void equals_shouldReturnTrue_whenComparingSameInstance() {
    Handle handle = new Handle("abc1234");

    assertThat(handle).isEqualTo(handle);
  }

  @Test
  void equals_shouldReturnTrue_whenValuesAreEqual() {
    Handle first = new Handle("abc1234");
    Handle second = new Handle("abc1234");

    assertThat(first).isEqualTo(second);
  }

  @Test
  void equals_shouldReturnTrue_whenValuesDifferOnlyByCase() {
    Handle first = new Handle("ABC1234");
    Handle second = new Handle("abc1234");

    assertThat(first).isEqualTo(second);
  }

  @Test
  void equals_shouldReturnTrue_whenValuesDifferOnlyBySurroundingWhitespace() {
    Handle first = new Handle("  abc1234  ");
    Handle second = new Handle("abc1234");

    assertThat(first).isEqualTo(second);
  }

  @Test
  void equals_shouldReturnFalse_whenValuesAreDifferent() {
    Handle first = new Handle("abc1234");
    Handle second = new Handle("xyz9876");

    assertThat(first).isNotEqualTo(second);
  }

  @Test
  void hashCode_shouldBeEqual_whenValuesAreEqual() {
    Handle first = new Handle("ABC1234");
    Handle second = new Handle("abc1234");

    assertThat(first.hashCode()).isEqualTo(second.hashCode());
  }

  @Test
  void equals_shouldReturnFalse_whenComparedWithDifferentType() {
    Handle handle = new Handle("abc1234");

    assertThat(handle).isNotEqualTo("abc1234");
  }

  @Test
  void hashCode_shouldBeConsistent_whenCalledMultipleTimes() {
    Handle handle = new Handle("abc1234");

    int firstHashCode = handle.hashCode();
    int secondHashCode = handle.hashCode();

    assertThat(secondHashCode).isEqualTo(firstHashCode);
  }

  @Test
  void toString_shouldReturnHandleValue() {
    Handle handle = new Handle("abc1234");

    assertThat(handle.toString()).isEqualTo("abc1234");
  }

  @Test
  void handleLength_shouldBeSeven() {
    assertThat(Handle.HANDLE_LENGTH).isEqualTo(7);
  }
}
