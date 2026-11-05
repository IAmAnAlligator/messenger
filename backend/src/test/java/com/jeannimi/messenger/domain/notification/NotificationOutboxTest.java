package com.jeannimi.messenger.domain.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.jeannimi.messenger.domain.chat.ChatId;
import com.jeannimi.messenger.domain.chat.ChatType;
import com.jeannimi.messenger.domain.common.EventId;
import com.jeannimi.messenger.domain.message.MessageId;
import com.jeannimi.messenger.domain.user.UserId;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class NotificationOutboxTest {

  private static final NotificationOutboxId OUTBOX_ID =
      new NotificationOutboxId(UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789001"));

  private static final NotificationOutboxId ANOTHER_OUTBOX_ID =
      new NotificationOutboxId(UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789002"));

  private static final EventId EVENT_ID =
      new EventId(UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789003"));

  private static final MessageId MESSAGE_ID =
      new MessageId(UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789004"));

  private static final ChatId CHAT_ID =
      new ChatId(UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789005"));

  private static final UserId RECIPIENT_USER_ID =
      new UserId(UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789006"));

  private static final UserId SENDER_USER_ID =
      new UserId(UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789007"));

  private static final Instant CREATED_AT = Instant.parse("2026-11-04T12:00:00Z");
  private static final Instant NEXT_ATTEMPT_AT = Instant.parse("2026-11-04T12:00:05Z");
  private static final Instant LOCKED_UNTIL = Instant.parse("2026-11-04T12:01:00Z");
  private static final Instant SENT_AT = Instant.parse("2026-11-04T12:02:00Z");

  private static final String CHAT_NAME = "Development team";
  private static final String SENDER_USERNAME = "alexey";

  // create

  @Test
  void create_shouldCreateNewOutbox() {
    NotificationOutbox outbox = createOutbox();

    assertAll(
        () -> assertEquals(OUTBOX_ID, outbox.getId()),
        () -> assertEquals(EVENT_ID, outbox.getEventId()),
        () -> assertEquals(MESSAGE_ID, outbox.getMessageId()),
        () -> assertEquals(CHAT_ID, outbox.getChatId()),
        () -> assertEquals(ChatType.GROUP, outbox.getChatType()),
        () -> assertEquals(CHAT_NAME, outbox.getChatName()),
        () -> assertEquals(RECIPIENT_USER_ID, outbox.getRecipientUserId()),
        () -> assertEquals(SENDER_USER_ID, outbox.getSenderUserId()),
        () -> assertEquals(SENDER_USERNAME, outbox.getSenderUsername()),
        () -> assertEquals(NotificationChannel.WEB_PUSH, outbox.getChannel()),
        () -> assertEquals(NotificationOutboxStatus.NEW, outbox.getStatus()),
        () -> assertEquals(0, outbox.getAttempts()),
        () -> assertEquals(CREATED_AT, outbox.getNextAttemptAt()),
        () -> assertEquals(CREATED_AT, outbox.getCreatedAt()),
        () -> assertNull(outbox.getLockedUntil()),
        () -> assertNull(outbox.getSentAt()),
        () -> assertNull(outbox.getLastError()));
  }

  @Test
  void create_shouldThrowNullPointerException_whenNowIsNull() {
    assertThrows(
        NullPointerException.class,
        () ->
            NotificationOutbox.create(
                OUTBOX_ID,
                EVENT_ID,
                MESSAGE_ID,
                CHAT_ID,
                ChatType.GROUP,
                CHAT_NAME,
                RECIPIENT_USER_ID,
                SENDER_USER_ID,
                SENDER_USERNAME,
                NotificationChannel.WEB_PUSH,
                null));
  }

  @Test
  void create_shouldThrowNullPointerException_whenIdIsNull() {
    assertThrows(
        NullPointerException.class,
        () ->
            NotificationOutbox.create(
                null,
                EVENT_ID,
                MESSAGE_ID,
                CHAT_ID,
                ChatType.GROUP,
                CHAT_NAME,
                RECIPIENT_USER_ID,
                SENDER_USER_ID,
                SENDER_USERNAME,
                NotificationChannel.WEB_PUSH,
                CREATED_AT));
  }

  @Test
  void create_shouldThrowNullPointerException_whenEventIdIsNull() {
    assertThrows(
        NullPointerException.class,
        () ->
            NotificationOutbox.create(
                OUTBOX_ID,
                null,
                MESSAGE_ID,
                CHAT_ID,
                ChatType.GROUP,
                CHAT_NAME,
                RECIPIENT_USER_ID,
                SENDER_USER_ID,
                SENDER_USERNAME,
                NotificationChannel.WEB_PUSH,
                CREATED_AT));
  }

  @Test
  void create_shouldThrowNullPointerException_whenMessageIdIsNull() {
    assertThrows(
        NullPointerException.class,
        () ->
            NotificationOutbox.create(
                OUTBOX_ID,
                EVENT_ID,
                null,
                CHAT_ID,
                ChatType.GROUP,
                CHAT_NAME,
                RECIPIENT_USER_ID,
                SENDER_USER_ID,
                SENDER_USERNAME,
                NotificationChannel.WEB_PUSH,
                CREATED_AT));
  }

  @Test
  void create_shouldThrowNullPointerException_whenChatIdIsNull() {
    assertThrows(
        NullPointerException.class,
        () ->
            NotificationOutbox.create(
                OUTBOX_ID,
                EVENT_ID,
                MESSAGE_ID,
                null,
                ChatType.GROUP,
                CHAT_NAME,
                RECIPIENT_USER_ID,
                SENDER_USER_ID,
                SENDER_USERNAME,
                NotificationChannel.WEB_PUSH,
                CREATED_AT));
  }

  @Test
  void create_shouldThrowNullPointerException_whenChatTypeIsNull() {
    assertThrows(
        NullPointerException.class,
        () ->
            NotificationOutbox.create(
                OUTBOX_ID,
                EVENT_ID,
                MESSAGE_ID,
                CHAT_ID,
                null,
                CHAT_NAME,
                RECIPIENT_USER_ID,
                SENDER_USER_ID,
                SENDER_USERNAME,
                NotificationChannel.WEB_PUSH,
                CREATED_AT));
  }

  @Test
  void create_shouldThrowNullPointerException_whenRecipientUserIdIsNull() {
    assertThrows(
        NullPointerException.class,
        () ->
            NotificationOutbox.create(
                OUTBOX_ID,
                EVENT_ID,
                MESSAGE_ID,
                CHAT_ID,
                ChatType.GROUP,
                CHAT_NAME,
                null,
                SENDER_USER_ID,
                SENDER_USERNAME,
                NotificationChannel.WEB_PUSH,
                CREATED_AT));
  }

  @Test
  void create_shouldThrowNullPointerException_whenSenderUserIdIsNull() {
    assertThrows(
        NullPointerException.class,
        () ->
            NotificationOutbox.create(
                OUTBOX_ID,
                EVENT_ID,
                MESSAGE_ID,
                CHAT_ID,
                ChatType.GROUP,
                CHAT_NAME,
                RECIPIENT_USER_ID,
                null,
                SENDER_USERNAME,
                NotificationChannel.WEB_PUSH,
                CREATED_AT));
  }

  @Test
  void create_shouldThrowNullPointerException_whenSenderUsernameIsNull() {
    assertThrows(
        NullPointerException.class,
        () ->
            NotificationOutbox.create(
                OUTBOX_ID,
                EVENT_ID,
                MESSAGE_ID,
                CHAT_ID,
                ChatType.GROUP,
                CHAT_NAME,
                RECIPIENT_USER_ID,
                SENDER_USER_ID,
                null,
                NotificationChannel.WEB_PUSH,
                CREATED_AT));
  }

  @Test
  void create_shouldThrowNullPointerException_whenChannelIsNull() {
    assertThrows(
        NullPointerException.class,
        () ->
            NotificationOutbox.create(
                OUTBOX_ID,
                EVENT_ID,
                MESSAGE_ID,
                CHAT_ID,
                ChatType.GROUP,
                CHAT_NAME,
                RECIPIENT_USER_ID,
                SENDER_USER_ID,
                SENDER_USERNAME,
                null,
                CREATED_AT));
  }

  // reconstitute

  @Test
  void reconstitute_shouldRestoreOutbox() {
    NotificationOutbox outbox =
        reconstituteOutbox(
            OUTBOX_ID,
            CHAT_NAME,
            NotificationOutboxStatus.NEW,
            2,
            NEXT_ATTEMPT_AT,
            LOCKED_UNTIL,
            CREATED_AT,
            SENT_AT,
            "Previous delivery attempt failed");

    assertAll(
        () -> assertEquals(OUTBOX_ID, outbox.getId()),
        () -> assertEquals(EVENT_ID, outbox.getEventId()),
        () -> assertEquals(MESSAGE_ID, outbox.getMessageId()),
        () -> assertEquals(CHAT_ID, outbox.getChatId()),
        () -> assertEquals(ChatType.GROUP, outbox.getChatType()),
        () -> assertEquals(CHAT_NAME, outbox.getChatName()),
        () -> assertEquals(RECIPIENT_USER_ID, outbox.getRecipientUserId()),
        () -> assertEquals(SENDER_USER_ID, outbox.getSenderUserId()),
        () -> assertEquals(SENDER_USERNAME, outbox.getSenderUsername()),
        () -> assertEquals(NotificationChannel.WEB_PUSH, outbox.getChannel()),
        () -> assertEquals(NotificationOutboxStatus.NEW, outbox.getStatus()),
        () -> assertEquals(2, outbox.getAttempts()),
        () -> assertEquals(NEXT_ATTEMPT_AT, outbox.getNextAttemptAt()),
        () -> assertEquals(LOCKED_UNTIL, outbox.getLockedUntil()),
        () -> assertEquals(CREATED_AT, outbox.getCreatedAt()),
        () -> assertEquals(SENT_AT, outbox.getSentAt()),
        () -> assertEquals("Previous delivery attempt failed", outbox.getLastError()));
  }

  @Test
  void reconstitute_shouldAllowNullOptionalFields() {
    NotificationOutbox outbox =
        reconstituteOutbox(
            OUTBOX_ID,
            null,
            NotificationOutboxStatus.NEW,
            0,
            NEXT_ATTEMPT_AT,
            null,
            CREATED_AT,
            null,
            null);

    assertAll(
        () -> assertNull(outbox.getChatName()),
        () -> assertNull(outbox.getLockedUntil()),
        () -> assertNull(outbox.getSentAt()),
        () -> assertNull(outbox.getLastError()));
  }

  @Test
  void reconstitute_shouldThrowNullPointerException_whenIdIsNull() {
    assertThrows(
        NullPointerException.class,
        () ->
            reconstituteOutbox(
                null,
                CHAT_NAME,
                NotificationOutboxStatus.NEW,
                0,
                NEXT_ATTEMPT_AT,
                null,
                CREATED_AT,
                null,
                null));
  }

  @Test
  void reconstitute_shouldThrowNullPointerException_whenNextAttemptAtIsNull() {
    assertThrows(
        NullPointerException.class,
        () ->
            reconstituteOutbox(
                OUTBOX_ID,
                CHAT_NAME,
                NotificationOutboxStatus.NEW,
                0,
                null,
                null,
                CREATED_AT,
                null,
                null));
  }

  @Test
  void reconstitute_shouldThrowNullPointerException_whenCreatedAtIsNull() {
    assertThrows(
        NullPointerException.class,
        () ->
            reconstituteOutbox(
                OUTBOX_ID,
                CHAT_NAME,
                NotificationOutboxStatus.NEW,
                0,
                NEXT_ATTEMPT_AT,
                null,
                null,
                null,
                null));
  }

  @Test
  void reconstitute_shouldThrowIllegalArgumentException_whenAttemptsIsNegative() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            reconstituteOutbox(
                OUTBOX_ID,
                CHAT_NAME,
                NotificationOutboxStatus.NEW,
                -1,
                NEXT_ATTEMPT_AT,
                null,
                CREATED_AT,
                null,
                null));
  }

  // equals

  @Test
  void equals_shouldReturnTrue_whenOutboxesHaveSameId() {
    NotificationOutbox first =
        reconstituteOutbox(
            OUTBOX_ID,
            CHAT_NAME,
            NotificationOutboxStatus.NEW,
            0,
            NEXT_ATTEMPT_AT,
            null,
            CREATED_AT,
            null,
            null);

    NotificationOutbox second =
        reconstituteOutbox(
            OUTBOX_ID,
            CHAT_NAME,
            NotificationOutboxStatus.NEW,
            3,
            NEXT_ATTEMPT_AT.plusSeconds(60),
            LOCKED_UNTIL,
            CREATED_AT,
            SENT_AT,
            "Some error");

    assertEquals(first, second);
  }

  @Test
  void equals_shouldReturnFalse_whenOutboxesHaveDifferentIds() {
    NotificationOutbox first =
        reconstituteOutbox(
            OUTBOX_ID,
            CHAT_NAME,
            NotificationOutboxStatus.NEW,
            0,
            NEXT_ATTEMPT_AT,
            null,
            CREATED_AT,
            null,
            null);

    NotificationOutbox second =
        reconstituteOutbox(
            ANOTHER_OUTBOX_ID,
            CHAT_NAME,
            NotificationOutboxStatus.NEW,
            0,
            NEXT_ATTEMPT_AT,
            null,
            CREATED_AT,
            null,
            null);

    assertNotEquals(first, second);
  }

  @Test
  void equals_shouldReturnTrue_whenComparingSameInstance() {
    NotificationOutbox outbox = createOutbox();

    assertEquals(outbox, outbox);
  }

  @Test
  void equals_shouldReturnFalse_whenComparingWithNull() {
    NotificationOutbox outbox = createOutbox();

    assertNotEquals(null, outbox);
  }

  @Test
  void equals_shouldReturnFalse_whenComparingWithDifferentType() {
    NotificationOutbox outbox = createOutbox();

    assertThat(outbox.equals("outbox")).isFalse();
  }

  // hashCode

  @Test
  void hashCode_shouldBeEqual_whenOutboxesHaveSameId() {
    NotificationOutbox first = createOutbox();

    NotificationOutbox second =
        reconstituteOutbox(
            OUTBOX_ID,
            CHAT_NAME,
            NotificationOutboxStatus.NEW,
            2,
            NEXT_ATTEMPT_AT,
            LOCKED_UNTIL,
            CREATED_AT,
            SENT_AT,
            "Some error");

    assertEquals(first.hashCode(), second.hashCode());
  }

  // helpers

  private static NotificationOutbox createOutbox() {
    return NotificationOutbox.create(
        OUTBOX_ID,
        EVENT_ID,
        MESSAGE_ID,
        CHAT_ID,
        ChatType.GROUP,
        CHAT_NAME,
        RECIPIENT_USER_ID,
        SENDER_USER_ID,
        SENDER_USERNAME,
        NotificationChannel.WEB_PUSH,
        CREATED_AT);
  }

  private static NotificationOutbox reconstituteOutbox(
      NotificationOutboxId id,
      String chatName,
      NotificationOutboxStatus status,
      int attempts,
      Instant nextAttemptAt,
      Instant lockedUntil,
      Instant createdAt,
      Instant sentAt,
      String lastError) {

    return NotificationOutbox.reconstitute(
        id,
        EVENT_ID,
        MESSAGE_ID,
        CHAT_ID,
        ChatType.GROUP,
        chatName,
        RECIPIENT_USER_ID,
        SENDER_USER_ID,
        SENDER_USERNAME,
        NotificationChannel.WEB_PUSH,
        status,
        attempts,
        nextAttemptAt,
        lockedUntil,
        createdAt,
        sentAt,
        lastError);
  }
}
