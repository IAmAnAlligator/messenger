package com.jeannimi.messenger.domain.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class SessionIdTest {

  private static final UUID SESSION_UUID = UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789012");

  private static final UUID ANOTHER_SESSION_UUID =
      UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789013");

  @Test
  void constructor_shouldCreateSessionId_whenValueIsValid() {
    SessionId sessionId = new SessionId(SESSION_UUID);

    assertThat(sessionId.value()).isEqualTo(SESSION_UUID);
  }

  @Test
  void constructor_shouldThrowNullPointerException_whenValueIsNull() {
    assertThatNullPointerException()
        .isThrownBy(() -> new SessionId(null))
        .withMessage("Session id must not be null");
  }

  @Test
  void equals_shouldReturnTrue_whenValuesAreEqual() {
    SessionId first = new SessionId(SESSION_UUID);
    SessionId second = new SessionId(SESSION_UUID);

    assertThat(first).isEqualTo(second);
  }

  @Test
  void equals_shouldReturnFalse_whenValuesAreDifferent() {
    SessionId first = new SessionId(SESSION_UUID);
    SessionId second = new SessionId(ANOTHER_SESSION_UUID);

    assertThat(first).isNotEqualTo(second);
  }

  @Test
  void hashCode_shouldBeEqual_whenValuesAreEqual() {
    SessionId first = new SessionId(SESSION_UUID);
    SessionId second = new SessionId(SESSION_UUID);

    assertThat(first.hashCode()).isEqualTo(second.hashCode());
  }
}
