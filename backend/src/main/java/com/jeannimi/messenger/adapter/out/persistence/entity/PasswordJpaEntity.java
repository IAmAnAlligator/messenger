package com.jeannimi.messenger.adapter.out.persistence.entity;

import com.jeannimi.messenger.domain.user.Password;
import com.jeannimi.messenger.domain.user.Username;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PasswordJpaEntity {

  @Column(name = "password_hash", nullable = false, length = 255)
  private String value;

  public PasswordJpaEntity(String value) {
    this.value = Objects.requireNonNull(value, "value");
  }

}
