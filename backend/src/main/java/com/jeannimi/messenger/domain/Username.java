package com.jeannimi.messenger.domain;

import jakarta.persistence.Embeddable;
import java.util.Objects;
import lombok.Getter;

@Getter
@Embeddable
public class Username {

  private String value;

  protected Username() {} // нужен для JPA

  public Username(String value) {

    if (value == null) {
      throw new IllegalArgumentException(
          "Username must not be blank"
      );
    }

    value = value.trim();

    if (value.isBlank()) {
      throw new IllegalArgumentException(
          "Username must not be blank"
      );
    }

    if (value.length() > 100) {
      throw new IllegalArgumentException(
          "Username too long"
      );
    }

    this.value = value;
  }

  // важно для JPA / коллекций
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof Username that)) return false;
    return value.equals(that.value);
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
