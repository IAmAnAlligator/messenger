package com.jeannimi.messenger.application.presence.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.jeannimi.messenger.domain.chat.ChatId;
import com.jeannimi.messenger.domain.user.UserId;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class UserPresenceServiceTest {

  private static final UserId USER_ID =
      new UserId(UUID.fromString("01912345-6789-7000-8000-000000000001"));

  private static final UserId ANOTHER_USER_ID =
      new UserId(UUID.fromString("01912345-6789-7000-8000-000000000002"));

  private static final ChatId CHAT_ID =
      new ChatId(UUID.fromString("01912345-6789-7000-8000-000000000003"));

  private static final ChatId ANOTHER_CHAT_ID =
      new ChatId(UUID.fromString("01912345-6789-7000-8000-000000000004"));

  private static final String SESSION_ID = "session-1";
  private static final String ANOTHER_SESSION_ID = "session-2";

  private UserPresenceService service;

  @BeforeEach
  void setUp() {
    service = new UserPresenceService();
  }

  // =========================
  // REGISTER SESSION
  // =========================

  @Test
  void registerSession_shouldReturnTrue_whenUserGoesOnline() {
    boolean result = service.registerSession(SESSION_ID, USER_ID);

    assertThat(result).isTrue();
    assertThat(service.isOnline(USER_ID)).isTrue();
  }

  @Test
  void registerSession_shouldReturnFalse_whenUserAlreadyOnline() {
    service.registerSession(SESSION_ID, USER_ID);

    boolean result = service.registerSession(ANOTHER_SESSION_ID, USER_ID);

    assertThat(result).isFalse();
    assertThat(service.isOnline(USER_ID)).isTrue();
  }

  @Test
  void registerSession_shouldAllowDifferentUsers() {
    boolean firstResult = service.registerSession(SESSION_ID, USER_ID);
    boolean secondResult = service.registerSession(ANOTHER_SESSION_ID, ANOTHER_USER_ID);

    assertThat(firstResult).isTrue();
    assertThat(secondResult).isTrue();

    assertThat(service.isOnline(USER_ID)).isTrue();
    assertThat(service.isOnline(ANOTHER_USER_ID)).isTrue();
  }

  @Test
  void registerSession_shouldThrowException_whenSessionIdIsNull() {
    assertThatNullPointerException()
        .isThrownBy(() -> service.registerSession(null, USER_ID))
        .withMessage("sessionId");
  }

  @Test
  void registerSession_shouldThrowException_whenUserIdIsNull() {
    assertThatNullPointerException()
        .isThrownBy(() -> service.registerSession(SESSION_ID, null))
        .withMessage("userId");
  }

  // =========================
  // UPDATE PRESENCE
  // =========================

  @Test
  void updatePresence_shouldSetVisibleChat() {
    service.registerSession(SESSION_ID, USER_ID);

    service.updatePresence(SESSION_ID, USER_ID, CHAT_ID, true);

    assertThat(service.isViewingChat(USER_ID, CHAT_ID)).isTrue();
    assertThat(service.isViewingChat(USER_ID, ANOTHER_CHAT_ID)).isFalse();
  }

  @Test
  void updatePresence_shouldUpdateVisibleChat() {
    service.registerSession(SESSION_ID, USER_ID);

    service.updatePresence(SESSION_ID, USER_ID, CHAT_ID, true);
    service.updatePresence(SESSION_ID, USER_ID, ANOTHER_CHAT_ID, true);

    assertThat(service.isViewingChat(USER_ID, CHAT_ID)).isFalse();
    assertThat(service.isViewingChat(USER_ID, ANOTHER_CHAT_ID)).isTrue();
  }

  @Test
  void updatePresence_shouldSetInvisible_whenVisibleIsFalse() {
    service.registerSession(SESSION_ID, USER_ID);
    service.updatePresence(SESSION_ID, USER_ID, CHAT_ID, true);

    service.updatePresence(SESSION_ID, USER_ID, null, false);

    assertThat(service.isOnline(USER_ID)).isTrue();
    assertThat(service.isViewingChat(USER_ID, CHAT_ID)).isFalse();
  }

  @Test
  void updatePresence_shouldAllowNullChatId_whenVisibleIsFalse() {
    service.registerSession(SESSION_ID, USER_ID);

    service.updatePresence(SESSION_ID, USER_ID, null, false);

    assertThat(service.isOnline(USER_ID)).isTrue();
  }

  @Test
  void updatePresence_shouldThrowException_whenSessionIsNotRegistered() {
    assertThatThrownBy(() -> service.updatePresence(SESSION_ID, USER_ID, CHAT_ID, true))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("WebSocket session is not registered");
  }

  @Test
  void updatePresence_shouldThrowException_whenUserDoesNotMatchSession() {
    service.registerSession(SESSION_ID, USER_ID);

    assertThatThrownBy(() -> service.updatePresence(SESSION_ID, ANOTHER_USER_ID, CHAT_ID, true))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("Session user does not match authenticated user");
  }

  @Test
  void updatePresence_shouldThrowException_whenChatIdIsNullAndVisibleIsTrue() {
    service.registerSession(SESSION_ID, USER_ID);

    assertThatNullPointerException()
        .isThrownBy(() -> service.updatePresence(SESSION_ID, USER_ID, null, true))
        .withMessage("Chat ID is required when visible");
  }

  @Test
  void updatePresence_shouldThrowException_whenSessionIdIsNull() {
    assertThatNullPointerException()
        .isThrownBy(() -> service.updatePresence(null, USER_ID, CHAT_ID, true))
        .withMessage("sessionId");
  }

  @Test
  void updatePresence_shouldThrowException_whenUserIdIsNull() {
    assertThatNullPointerException()
        .isThrownBy(() -> service.updatePresence(SESSION_ID, null, CHAT_ID, true))
        .withMessage("userId");
  }

  // =========================
  // REMOVE SESSION
  // =========================

  @Test
  void removeSession_shouldReturnUserId_whenLastSessionIsRemoved() {
    service.registerSession(SESSION_ID, USER_ID);

    Optional<UserId> result = service.removeSession(SESSION_ID);

    assertThat(result).contains(USER_ID);
    assertThat(service.isOnline(USER_ID)).isFalse();
  }

  @Test
  void removeSession_shouldReturnEmpty_whenUserHasAnotherSession() {
    service.registerSession(SESSION_ID, USER_ID);
    service.registerSession(ANOTHER_SESSION_ID, USER_ID);

    Optional<UserId> result = service.removeSession(SESSION_ID);

    assertThat(result).isEmpty();
    assertThat(service.isOnline(USER_ID)).isTrue();
  }

  @Test
  void removeSession_shouldReturnUserId_whenLastRemainingSessionIsRemoved() {
    service.registerSession(SESSION_ID, USER_ID);
    service.registerSession(ANOTHER_SESSION_ID, USER_ID);

    service.removeSession(SESSION_ID);

    Optional<UserId> result = service.removeSession(ANOTHER_SESSION_ID);

    assertThat(result).contains(USER_ID);
    assertThat(service.isOnline(USER_ID)).isFalse();
  }

  @Test
  void removeSession_shouldReturnEmpty_whenSessionDoesNotExist() {
    Optional<UserId> result = service.removeSession(SESSION_ID);

    assertThat(result).isEmpty();
  }

  @Test
  void removeSession_shouldReturnEmpty_whenSessionIdIsNull() {
    Optional<UserId> result = service.removeSession(null);

    assertThat(result).isEmpty();
  }

  // =========================
  // IS ONLINE
  // =========================

  @Test
  void isOnline_shouldReturnTrue_whenUserHasSession() {
    service.registerSession(SESSION_ID, USER_ID);

    assertThat(service.isOnline(USER_ID)).isTrue();
  }

  @Test
  void isOnline_shouldReturnFalse_whenUserHasNoSession() {
    assertThat(service.isOnline(USER_ID)).isFalse();
  }

  @Test
  void isOnline_shouldReturnFalse_whenAnotherUserHasSession() {
    service.registerSession(SESSION_ID, ANOTHER_USER_ID);

    assertThat(service.isOnline(USER_ID)).isFalse();
  }

  @Test
  void isOnline_shouldThrowException_whenUserIdIsNull() {
    assertThatNullPointerException().isThrownBy(() -> service.isOnline(null)).withMessage("userId");
  }

  // =========================
  // IS VIEWING CHAT
  // =========================

  @Test
  void isViewingChat_shouldReturnTrue_whenUserViewsChat() {
    service.registerSession(SESSION_ID, USER_ID);
    service.updatePresence(SESSION_ID, USER_ID, CHAT_ID, true);

    assertThat(service.isViewingChat(USER_ID, CHAT_ID)).isTrue();
  }

  @Test
  void isViewingChat_shouldReturnFalse_whenUserDoesNotViewChat() {
    service.registerSession(SESSION_ID, USER_ID);

    assertThat(service.isViewingChat(USER_ID, CHAT_ID)).isFalse();
  }

  @Test
  void isViewingChat_shouldReturnFalse_whenUserViewsAnotherChat() {
    service.registerSession(SESSION_ID, USER_ID);
    service.updatePresence(SESSION_ID, USER_ID, ANOTHER_CHAT_ID, true);

    assertThat(service.isViewingChat(USER_ID, CHAT_ID)).isFalse();
  }

  @Test
  void isViewingChat_shouldReturnFalse_whenAnotherUserViewsChat() {
    service.registerSession(SESSION_ID, ANOTHER_USER_ID);
    service.updatePresence(SESSION_ID, ANOTHER_USER_ID, CHAT_ID, true);

    assertThat(service.isViewingChat(USER_ID, CHAT_ID)).isFalse();
  }

  @Test
  void isViewingChat_shouldReturnTrue_whenOneOfMultipleSessionsViewsChat() {
    service.registerSession(SESSION_ID, USER_ID);
    service.registerSession(ANOTHER_SESSION_ID, USER_ID);

    service.updatePresence(ANOTHER_SESSION_ID, USER_ID, CHAT_ID, true);

    assertThat(service.isViewingChat(USER_ID, CHAT_ID)).isTrue();
  }

  @Test
  void isViewingChat_shouldReturnFalse_whenViewingSessionIsRemoved() {
    service.registerSession(SESSION_ID, USER_ID);
    service.updatePresence(SESSION_ID, USER_ID, CHAT_ID, true);

    service.removeSession(SESSION_ID);

    assertThat(service.isViewingChat(USER_ID, CHAT_ID)).isFalse();
  }

  @Test
  void isViewingChat_shouldThrowException_whenUserIdIsNull() {
    assertThatNullPointerException()
        .isThrownBy(() -> service.isViewingChat(null, CHAT_ID))
        .withMessage("userId");
  }

  @Test
  void isViewingChat_shouldThrowException_whenChatIdIsNull() {
    assertThatNullPointerException()
        .isThrownBy(() -> service.isViewingChat(USER_ID, null))
        .withMessage("chatId");
  }

  @Test
  void registerSession_shouldThrowException_whenSessionIsAlreadyRegistered() {
    service.registerSession(SESSION_ID, USER_ID);

    assertThatThrownBy(() -> service.registerSession(SESSION_ID, USER_ID))
        .isInstanceOf(IllegalStateException.class)
        .hasMessage("WebSocket session is already registered");

    assertThat(service.isOnline(USER_ID)).isTrue();
  }

  @Test
  void registerSession_shouldThrowException_whenSessionIdIsBlank() {
    assertThatThrownBy(() -> service.registerSession(" ", USER_ID))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("sessionId must not be blank");
  }

  @Test
  void registerSession_shouldThrowException_whenSessionIdIsEmpty() {
    assertThatThrownBy(() -> service.registerSession("", USER_ID))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("sessionId must not be blank");
  }

  @Test
  void updatePresence_shouldThrowException_whenSessionIdIsBlank() {
    assertThatThrownBy(() -> service.updatePresence(" ", USER_ID, CHAT_ID, true))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("sessionId must not be blank");
  }

  @Test
  void updatePresence_shouldThrowException_whenSessionIdIsEmpty() {
    assertThatThrownBy(() -> service.updatePresence("", USER_ID, CHAT_ID, true))
        .isInstanceOf(IllegalArgumentException.class)
        .hasMessage("sessionId must not be blank");
  }
}
