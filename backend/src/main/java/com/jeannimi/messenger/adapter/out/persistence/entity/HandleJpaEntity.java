package com.jeannimi.messenger.adapter.out.persistence.entity;

import com.jeannimi.messenger.domain.user.Handle;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Embeddable
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class HandleJpaEntity {

  @Column(name = "handle", nullable = false, length = Handle.HANDLE_LENGTH)
  private String value;

  public HandleJpaEntity(String value) {
    this.value = Objects.requireNonNull(value, "value");
  }
}
