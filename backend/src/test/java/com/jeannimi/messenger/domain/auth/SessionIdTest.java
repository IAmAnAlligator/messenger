package com.jeannimi.messenger.domain.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class SessionIdTest {

  @Test
  void constructor_shouldCreateSessionId_whenValueIsValid() {
    UUID value = UUID.randomUUID();

    SessionId sessionId = new SessionId(value);

    assertThat(sessionId.value()).isEqualTo(value);
  }

  @Test
  void constructor_shouldThrowNullPointerException_whenValueIsNull() {
    assertThatNullPointerException()
        .isThrownBy(() -> new SessionId(null))
        .withMessage("Session id must not be null");
  }

  @Test
  void equals_shouldReturnTrue_whenValuesAreEqual() {
    UUID value = UUID.randomUUID();

    SessionId first = new SessionId(value);
    SessionId second = new SessionId(value);

    assertThat(first).isEqualTo(second);
  }

  @Test
  void equals_shouldReturnFalse_whenValuesAreDifferent() {
    SessionId first = new SessionId(UUID.randomUUID());
    SessionId second = new SessionId(UUID.randomUUID());

    assertThat(first).isNotEqualTo(second);
  }

  @Test
  void hashCode_shouldBeEqual_whenValuesAreEqual() {
    UUID value = UUID.randomUUID();

    SessionId first = new SessionId(value);
    SessionId second = new SessionId(value);

    assertThat(first.hashCode()).isEqualTo(second.hashCode());
  }
}
