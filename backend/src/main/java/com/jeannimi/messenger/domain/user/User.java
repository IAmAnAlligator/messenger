package com.jeannimi.messenger.domain.user;

import java.time.Instant;
import java.util.Objects;
import lombok.Getter;

@Getter
public final class User {

  private final Long id;
  private final Username username;
  private final Email email;
  private final String passwordHash;
  private final Role role;
  private final Instant createdAt;

  private User(Long id, Username username, Email email, String passwordHash, Role role, Instant createdAt) {

    this.id = id;
    this.username = Objects.requireNonNull(username, "username");
    this.email = Objects.requireNonNull(email, "email");
    this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash");
    this.role = Objects.requireNonNull(role, "role");
    this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
  }

  public static User create(Username username, Email email, String passwordHash, Role role) {

    return new User(null, username, email, passwordHash, role, Instant.now());
  }

  public static User reconstitute(
      Long id, Username username, Email email, String passwordHash, Role role, Instant createdAt) {

    return new User(Objects.requireNonNull(id, "id"), username, email, passwordHash, role, createdAt);
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
