package com.jeannimi.messenger.domain.user;

import lombok.Getter;

@Getter
public final class Username {

  public static final int MAX_USERNAME_LENGTH = 100;
  public static final int MIN_USERNAME_LENGTH = 3;

  private final String value;

  public Username(String value) {

    if (value == null) {
      throw new IllegalArgumentException("Username must not be null");
    }

    value = value.trim();

    if (value.isBlank()) {
      throw new IllegalArgumentException("Username must not be blank");
    }

    if (value.length() < MIN_USERNAME_LENGTH) {
      throw new IllegalArgumentException("Username too short");
    }

    if (value.length() > MAX_USERNAME_LENGTH) {
      throw new IllegalArgumentException("Username too long");
    }

    if (containsControlCharacter(value)) {
      throw new IllegalArgumentException(
          "Username contains invalid characters");
    }

    this.value = value;
  }

  private static boolean containsControlCharacter(
      String value) {

    return value.codePoints()
        .anyMatch(Character::isISOControl);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof Username that)) {
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
    return value;
  }
}
