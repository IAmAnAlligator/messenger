package com.jeannimi.messenger.domain.user;

import java.time.Instant;
import java.util.Objects;
import lombok.Getter;

@Getter
public final class User {

  private final UserId id;
  private final Handle handle;
  private final Username username;
  private final Email email;
  private final PasswordHash passwordHash;
  private final Role role;
  private final Instant createdAt;

  private User(
      UserId id,
      Handle handle,
      Username username,
      Email email,
      PasswordHash passwordHash,
      Role role,
      Instant createdAt) {

    this.id = Objects.requireNonNull(id, "id");
    this.handle = Objects.requireNonNull(handle, "handle");
    this.username = Objects.requireNonNull(username, "username");
    this.email = Objects.requireNonNull(email, "email");
    this.passwordHash = Objects.requireNonNull(passwordHash, "passwordHash");
    this.role = Objects.requireNonNull(role, "role");
    this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
  }

  public static User create(
      UserId id,
      Handle handle,
      Username username,
      Email email,
      PasswordHash passwordHash,
      Role role) {

    return new User(id, handle, username, email, passwordHash, role, Instant.now());
  }

  public static User reconstitute(
      UserId id,
      Handle handle,
      Username username,
      Email email,
      PasswordHash passwordHash,
      Role role,
      Instant createdAt) {

    return new User(id, handle, username, email, passwordHash, role, createdAt);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }

    if (!(o instanceof User that)) {
      return false;
    }

    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
