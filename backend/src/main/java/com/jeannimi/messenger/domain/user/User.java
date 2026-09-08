package com.jeannimi.messenger.domain.user;

import java.time.Instant;
import java.util.Objects;
import lombok.Getter;

@Getter
public final class User {

  private final Long id;
  private Username username;
  private String passwordHash;
  private Role role;
  private final Instant createdAt;

  private User(Long id, Username username, String passwordHash, Role role, Instant createdAt) {

    this.id = id;
    this.username = Objects.requireNonNull(username, "username");
    this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash");
    this.role = Objects.requireNonNull(role, "role");
    this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
  }

  public static User create(Username username, String passwordHash, Role role) {

    return new User(null, username, passwordHash, role, Instant.now());
  }

  public static User reconstitute(
      Long id, Username username, String passwordHash, Role role, Instant createdAt) {

    return new User(Objects.requireNonNull(id, "id"), username, passwordHash, role, createdAt);
  }

  public boolean isUser() {
    return role == Role.USER;
  }

  public void changeRole(Role newRole) {
    this.role = Objects.requireNonNull(newRole, "role");
  }

  public void changeUsername(Username username) {
    this.username = Objects.requireNonNull(username, "username");
  }

  public void changePassword(String encodedPassword) {
    this.passwordHash = Objects.requireNonNull(encodedPassword, "passwordHash");
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof User that)) {
      return false;
    }

    return id != null && id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
