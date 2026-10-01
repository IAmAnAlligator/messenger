package com.jeannimi.messenger.domain.user;

import lombok.Getter;

@Getter
public final class Password {

  public static final int MIN_PASSWORD_LENGTH = 6;
  public static final int MAX_PASSWORD_LENGTH = 30;

  private final String value;

  public Password(String value) {

    if (value == null) {
      throw new IllegalArgumentException("Password must not be null");
    }

    if (value.isBlank()) {
      throw new IllegalArgumentException("Password must not be blank");
    }

    if (value.length() < MIN_PASSWORD_LENGTH) {
      throw new IllegalArgumentException("Password too short");
    }

    if (value.length() > MAX_PASSWORD_LENGTH) {
      throw new IllegalArgumentException("Password too long");
    }

    if (containsControlCharacter(value)) {
      throw new IllegalArgumentException("Password contains invalid characters");
    }

    this.value = value;
  }

  private static boolean containsControlCharacter(String value) {

    return value.codePoints().anyMatch(Character::isISOControl);
  }

  @Override
  public boolean equals(Object o) {

    if (this == o) {
      return true;
    }

    if (!(o instanceof Password that)) {
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
