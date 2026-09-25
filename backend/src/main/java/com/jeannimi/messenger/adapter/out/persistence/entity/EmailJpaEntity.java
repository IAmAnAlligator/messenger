package com.jeannimi.messenger.adapter.out.persistence.entity;

import com.jeannimi.messenger.domain.user.Email;
import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailJpaEntity {

  @Column(name = "email", nullable = false, length = Email.MAX_EMAIL_LENGTH)
  private String value;

  public EmailJpaEntity(String value) {
    this.value = value;
  }
}