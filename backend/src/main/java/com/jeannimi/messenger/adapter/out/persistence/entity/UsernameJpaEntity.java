package com.jeannimi.messenger.adapter.out.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UsernameJpaEntity {

  @Column(name = "username", nullable = false, length = 100)
  private String value;

  public UsernameJpaEntity(String value) {
    this.value = Objects.requireNonNull(value, "value");
  }
}
