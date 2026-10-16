package com.jeannimi.messenger.domain.user;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserTest {

  private static final UUID USER_UUID = UUID.fromString("01a1116e-427f-707b-8c4b-f2d4b371d24e");

  private static final UUID ANOTHER_USER_UUID =
      UUID.fromString("01a1116f-e068-7339-a582-630ab575df51");

  private static final Instant CREATED_AT = Instant.parse("2026-01-01T10:00:00Z");

  private UserId userId;
  private Handle handle;
  private Username username;
  private Email email;
  private PasswordHash passwordHash;
  private Role role;
  private Instant createdAt;

  @BeforeEach
  void setUp() {
    userId = new UserId(USER_UUID);
    handle = new Handle("abc1234");
    username = new Username("Alex");
    email = new Email("alex@example.com");
    passwordHash = new PasswordHash("$2a$12$exampleHash");
    role = Role.USER;
    createdAt = CREATED_AT;
  }

  @Test
  void create_shouldCreateUserWithProvidedFields() {
    User user = User.create(userId, handle, username, email, passwordHash, role);

    assertThat(user.getId()).isEqualTo(userId);
    assertThat(user.getHandle()).isEqualTo(handle);
    assertThat(user.getUsername()).isEqualTo(username);
    assertThat(user.getEmail()).isEqualTo(email);
    assertThat(user.getPasswordHash()).isEqualTo(passwordHash);
    assertThat(user.getRole()).isEqualTo(role);
    assertThat(user.getCreatedAt()).isNotNull();
  }

  @Test
  void create_shouldSetCreatedAtToCurrentTime() {
    Instant beforeCreation = Instant.now();

    User user = User.create(userId, handle, username, email, passwordHash, role);

    Instant afterCreation = Instant.now();

    assertThat(user.getCreatedAt()).isBetween(beforeCreation, afterCreation);
  }

  @Test
  void create_shouldThrowNullPointerException_whenIdIsNull() {
    assertThatNullPointerException()
        .isThrownBy(() -> User.create(null, handle, username, email, passwordHash, role))
        .withMessage("id");
  }

  @Test
  void create_shouldThrowNullPointerException_whenHandleIsNull() {
    assertThatNullPointerException()
        .isThrownBy(() -> User.create(userId, null, username, email, passwordHash, role))
        .withMessage("handle");
  }

  @Test
  void create_shouldThrowNullPointerException_whenUsernameIsNull() {
    assertThatNullPointerException()
        .isThrownBy(() -> User.create(userId, handle, null, email, passwordHash, role))
        .withMessage("username");
  }

  @Test
  void create_shouldThrowNullPointerException_whenEmailIsNull() {
    assertThatNullPointerException()
        .isThrownBy(() -> User.create(userId, handle, username, null, passwordHash, role))
        .withMessage("email");
  }

  @Test
  void create_shouldThrowNullPointerException_whenPasswordHashIsNull() {
    assertThatNullPointerException()
        .isThrownBy(() -> User.create(userId, handle, username, email, null, role))
        .withMessage("passwordHash");
  }

  @Test
  void create_shouldThrowNullPointerException_whenRoleIsNull() {
    assertThatNullPointerException()
        .isThrownBy(() -> User.create(userId, handle, username, email, passwordHash, null))
        .withMessage("role");
  }

  @Test
  void reconstitute_shouldRestoreUserWithProvidedFields() {
    User user = User.reconstitute(userId, handle, username, email, passwordHash, role, createdAt);

    assertThat(user.getId()).isEqualTo(userId);
    assertThat(user.getHandle()).isEqualTo(handle);
    assertThat(user.getUsername()).isEqualTo(username);
    assertThat(user.getEmail()).isEqualTo(email);
    assertThat(user.getPasswordHash()).isEqualTo(passwordHash);
    assertThat(user.getRole()).isEqualTo(role);
    assertThat(user.getCreatedAt()).isEqualTo(createdAt);
  }

  @Test
  void reconstitute_shouldThrowNullPointerException_whenCreatedAtIsNull() {
    assertThatNullPointerException()
        .isThrownBy(
            () -> User.reconstitute(userId, handle, username, email, passwordHash, role, null))
        .withMessage("createdAt");
  }

  @Test
  void equals_shouldReturnTrue_whenComparingSameInstance() {
    User user = createUser();

    assertThat(user.equals(user)).isTrue();
  }

  @Test
  void equals_shouldReturnTrue_whenUsersHaveSameId() {
    User first = createUser();

    User second =
        User.reconstitute(
            userId,
            new Handle("xyz9876"),
            new Username("Other"),
            new Email("other@example.com"),
            new PasswordHash("$2a$12$anotherHash"),
            Role.USER,
            createdAt.plusSeconds(60));

    assertThat(first).isEqualTo(second);
  }

  @Test
  void equals_shouldReturnFalse_whenUsersHaveDifferentIds() {
    User first = createUser();

    User second =
        User.reconstitute(
            new UserId(ANOTHER_USER_UUID), handle, username, email, passwordHash, role, createdAt);

    assertThat(first).isNotEqualTo(second);
  }

  @Test
  void equals_shouldReturnFalse_whenComparingWithNull() {
    User user = createUser();

    assertThat(user.equals(null)).isFalse();
  }

  @Test
  void equals_shouldReturnFalse_whenComparingWithDifferentType() {
    User user = createUser();

    assertThat(user.equals("user")).isFalse();
  }

  @Test
  void hashCode_shouldBeEqual_whenUsersHaveSameId() {
    User first = createUser();

    User second =
        User.reconstitute(
            userId,
            new Handle("xyz9876"),
            new Username("Other"),
            new Email("other@example.com"),
            new PasswordHash("$2a$12$anotherHash"),
            Role.USER,
            createdAt.plusSeconds(60));

    assertThat(first.hashCode()).isEqualTo(second.hashCode());
  }

  @Test
  void hashCode_shouldBeConsistent_whenCalledMultipleTimes() {
    User user = createUser();

    int firstHashCode = user.hashCode();
    int secondHashCode = user.hashCode();

    assertThat(secondHashCode).isEqualTo(firstHashCode);
  }

  private User createUser() {
    return User.reconstitute(userId, handle, username, email, passwordHash, role, createdAt);
  }
}
