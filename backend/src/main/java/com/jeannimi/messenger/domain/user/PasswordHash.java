package com.jeannimi.messenger.domain.user;

import lombok.Getter;

@Getter
public final class PasswordHash {

  private final String value;

  public PasswordHash(String value) {

    if (value == null) {
      throw new IllegalArgumentException(
          "Password hash must not be null");
    }

    if (value.isBlank()) {
      throw new IllegalArgumentException(
          "Password hash must not be blank");
    }

    if (containsControlCharacter(value)) {
      throw new IllegalArgumentException(
          "Password hash contains invalid characters");
    }

    this.value = value;
  }

  private static boolean containsControlCharacter(String value) {
    return value.codePoints()
        .anyMatch(Character::isISOControl);
  }

  @Override
  public boolean equals(Object o) {

    if (this == o) {
      return true;
    }

    if (!(o instanceof PasswordHash that)) {
      return false;
    }

    return value.equals(that.value);
  }

  @Override
  public int hashCode() {
    return value.hashCode();
  }

  @Override
  public String toString() {
    return "********";
  }
}