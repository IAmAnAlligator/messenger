package com.jeannimi.messenger.adapter.out.persistence.entity;

import com.jeannimi.messenger.domain.user.Role;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Embedded private UsernameJpaEntity username;

  @Column(name = "password", nullable = false, length = 255)
  private String password;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false, length = 50)
  private Role role;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  public UserJpaEntity(UsernameJpaEntity username, String password, Role role, Instant createdAt) {

    this.username = username;
    this.password = password;
    this.role = role;
    this.createdAt = createdAt;
  }
}
