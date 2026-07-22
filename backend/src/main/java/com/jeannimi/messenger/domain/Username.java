package com.jeannimi.messenger.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Username {

  private static final int MAX_USERNAME_LENGTH = 100;

  @Column(nullable = false, length = MAX_USERNAME_LENGTH)
  private String value;

  public Username(String value) {

    if (value == null) {
      throw new IllegalArgumentException("Username must not be null");
    }

    value = value.trim();

    if (value.isBlank()) {
      throw new IllegalArgumentException("Username must not be blank");
    }

    if (value.length() > MAX_USERNAME_LENGTH) {
      throw new IllegalArgumentException("Username too long");
    }

    this.value = value;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Username that)) return false;
    return Objects.equals(value, that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(value);
  }

  @Override
  public String toString() {
    return value;
  }
}
