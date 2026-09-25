package com.jeannimi.messenger.domain.user;

import java.util.Locale;
import java.util.Objects;
import java.util.regex.Pattern;
import lombok.Getter;

@Getter
public final class Email {

  public static final int MAX_EMAIL_LENGTH = 255;

  private static final Pattern EMAIL_PATTERN =
      Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

  private final String value;

  public Email(String value) {

    if (value == null) {
      throw new IllegalArgumentException("Email must not be null");
    }

    value = value.trim().toLowerCase(Locale.ROOT);

    if (value.isEmpty()) {
      throw new IllegalArgumentException("Email must not be blank");
    }

    if (value.length() > MAX_EMAIL_LENGTH) {
      throw new IllegalArgumentException("Email must not exceed 255 characters");
    }

    if (!EMAIL_PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException("Invalid email");
    }

    this.value = value;
  }

  @Override
  public boolean equals(Object o) {

    if (this == o) {
      return true;
    }

    if (!(o instanceof Email that)) {
      return false;
    }

    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value);
  }

  @Override
  public String toString() {
    return value;
  }
}