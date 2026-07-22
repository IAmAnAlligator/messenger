package com.jeannimi.messenger.entity;

import com.jeannimi.messenger.domain.Username;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(
    name = "users",
    indexes = {
      @Index(name = "idx_username", columnList = "username")
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Embedded
  @AttributeOverride(name = "value", column = @Column(name = "username", unique = true))
  private Username username;

  @Column(name = "password", nullable = false)
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false)
  private Role role;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @PrePersist
  private void prePersist() {
    if (createdAt == null) {
      createdAt = Instant.now();
    }
  }

  public boolean isUser() {
    return role == Role.USER;
  }

  public void changeRole(Role newRole) {
    this.role = Objects.requireNonNull(newRole);
  }

  public void changeUsername(Username username) {
    this.username = Objects.requireNonNull(username, "username");
  }

  public void changePassword(String encodedPassword) {
    this.passwordHash = Objects.requireNonNull(encodedPassword, "encodedPassword");
  }

  public static User of(Username username, String encodedPassword, Role role) {
    User user = new User();
    user.username = Objects.requireNonNull(username, "username");
    user.passwordHash = Objects.requireNonNull(encodedPassword, "encodedPassword");
    user.role = Objects.requireNonNull(role, "role");
    return user;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof User that)) return false;
    return id != null && id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
