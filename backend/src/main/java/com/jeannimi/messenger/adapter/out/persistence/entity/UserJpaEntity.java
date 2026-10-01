package com.jeannimi.messenger.adapter.out.persistence.entity;

import com.jeannimi.messenger.domain.user.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserJpaEntity {

  @Id
  @Column(name = "id", nullable = false, updatable = false)
  private UUID id;

  @Embedded private HandleJpaEntity handle;

  @Embedded private UsernameJpaEntity username;

  @Embedded private EmailJpaEntity email;

  @Embedded private PasswordJpaEntity password;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false, length = 50)
  private Role role;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  public UserJpaEntity(
      UUID id,
      HandleJpaEntity handle,
      UsernameJpaEntity username,
      EmailJpaEntity email,
      PasswordJpaEntity password,
      Role role,
      Instant createdAt) {

    this.id = id;
    this.handle = handle;
    this.username = username;
    this.email = email;
    this.password = password;
    this.role = role;
    this.createdAt = createdAt;
  }
}
