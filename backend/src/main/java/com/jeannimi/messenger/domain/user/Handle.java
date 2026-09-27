package com.jeannimi.messenger.domain.user;

import java.util.Locale;
import java.util.regex.Pattern;
import lombok.Getter;

@Getter
public final class Handle {

  public static final int HANDLE_LENGTH = 7;

  private static final Pattern HANDLE_PATTERN =
      Pattern.compile("[a-z0-9]{" + HANDLE_LENGTH + "}");

  private final String value;

  public Handle(String value) {

    if (value == null) {
      throw new IllegalArgumentException("Handle must not be null");
    }

    value = value.trim().toLowerCase(Locale.ROOT);

    if (!HANDLE_PATTERN.matcher(value).matches()) {
      throw new IllegalArgumentException(
          "Handle must contain exactly "
              + HANDLE_LENGTH
              + " lowercase letters or digits");
    }

    this.value = value;
  }

  public String asTag() {
    return "@" + value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof Handle that)) {
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