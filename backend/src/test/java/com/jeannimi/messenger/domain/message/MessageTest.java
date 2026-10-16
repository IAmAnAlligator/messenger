package com.jeannimi.messenger.domain.message;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import com.jeannimi.messenger.domain.chat.ChatId;
import com.jeannimi.messenger.domain.exception.MessageException;
import com.jeannimi.messenger.domain.user.UserId;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MessageTest {

  private static final UUID MESSAGE_UUID = UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789012");
  private static final UUID ANOTHER_MESSAGE_UUID =
      UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789015");
  private static final UUID CHAT_UUID = UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789013");
  private static final UUID USER_UUID = UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789014");
  private static final UUID FILE_ATTACHMENT_UUID =
      UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789014");

  private static final Instant CREATED_AT = Instant.parse("2026-01-01T10:00:00Z");

  private MessageId messageId;
  private ChatId chatId;
  private UserId senderId;
  private FileAttachment attachment;

  @BeforeEach
  void setUp() {
    messageId = new MessageId(MESSAGE_UUID);
    chatId = new ChatId(CHAT_UUID);
    senderId = new UserId(USER_UUID);

    FileAttachmentId fileAttachmentId = new FileAttachmentId(FILE_ATTACHMENT_UUID);

    attachment =
        FileAttachment.create(
            fileAttachmentId,
            "stored-document.pdf",
            "application/pdf",
            "storage/files/stored-document.pdf",
            1024L);
  }

  @Test
  void ofText_shouldCreateTextMessage_whenContentIsValid() {
    String content = "Hello, world!";

    Message message = Message.ofText(messageId, chatId, senderId, content);

    assertEquals(messageId, message.getId());
    assertEquals(chatId, message.getChatId());
    assertEquals(senderId, message.getSenderId());
    assertEquals(content, message.getContent());
    assertNotNull(message.getCreatedAt());
    assertEquals(MessageType.TEXT, message.getType());
    assertNull(message.getAttachment());
  }

  @Test
  void ofText_shouldTrimContent_whenContentContainsWhitespace() {
    Message message = Message.ofText(messageId, chatId, senderId, "  Hello, world!  ");

    assertEquals("Hello, world!", message.getContent());
  }

  @Test
  void ofText_shouldAcceptContentAtMaximumLength() {
    String content = "a".repeat(MessageConstants.MAX_CONTENT_LENGTH);

    Message message = Message.ofText(messageId, chatId, senderId, content);

    assertEquals(content, message.getContent());
  }

  @Test
  void ofText_shouldThrowMessageException_whenContentIsBlank() {
    assertThrows(MessageException.class, () -> Message.ofText(messageId, chatId, senderId, "   "));
  }

  @Test
  void ofText_shouldThrowMessageException_whenContentIsEmpty() {
    assertThrows(MessageException.class, () -> Message.ofText(messageId, chatId, senderId, ""));
  }

  @Test
  void ofText_shouldThrowNullPointerException_whenContentIsNull() {
    assertThrows(
        NullPointerException.class, () -> Message.ofText(messageId, chatId, senderId, null));
  }

  @Test
  void ofText_shouldThrowMessageException_whenContentExceedsMaximumLength() {
    String content = "a".repeat(MessageConstants.MAX_CONTENT_LENGTH + 1);

    assertThrows(
        MessageException.class, () -> Message.ofText(messageId, chatId, senderId, content));
  }

  @Test
  void ofText_shouldThrowNullPointerException_whenMessageIdIsNull() {
    assertThrows(NullPointerException.class, () -> Message.ofText(null, chatId, senderId, "Hello"));
  }

  @Test
  void ofText_shouldThrowNullPointerException_whenChatIdIsNull() {
    assertThrows(
        NullPointerException.class, () -> Message.ofText(messageId, null, senderId, "Hello"));
  }

  @Test
  void ofText_shouldThrowNullPointerException_whenSenderIdIsNull() {
    assertThrows(
        NullPointerException.class, () -> Message.ofText(messageId, chatId, null, "Hello"));
  }

  @Test
  void ofFile_shouldCreateFileMessage_whenAttachmentIsValid() {
    Message message = Message.ofFile(messageId, chatId, senderId, attachment);

    assertEquals(messageId, message.getId());
    assertEquals(chatId, message.getChatId());
    assertEquals(senderId, message.getSenderId());
    assertNull(message.getContent());
    assertNotNull(message.getCreatedAt());
    assertEquals(MessageType.FILE, message.getType());
    assertSame(attachment, message.getAttachment());
  }

  @Test
  void ofFile_shouldThrowNullPointerException_whenMessageIdIsNull() {
    assertThrows(
        NullPointerException.class, () -> Message.ofFile(null, chatId, senderId, attachment));
  }

  @Test
  void ofFile_shouldThrowNullPointerException_whenChatIdIsNull() {
    assertThrows(
        NullPointerException.class, () -> Message.ofFile(messageId, null, senderId, attachment));
  }

  @Test
  void ofFile_shouldThrowNullPointerException_whenSenderIdIsNull() {
    assertThrows(
        NullPointerException.class, () -> Message.ofFile(messageId, chatId, null, attachment));
  }

  @Test
  void ofFile_shouldThrowMessageException_whenAttachmentIsNull() {
    assertThrows(MessageException.class, () -> Message.ofFile(messageId, chatId, senderId, null));
  }

  @Test
  void reconstitute_shouldRestoreTextMessage_whenDataIsValid() {
    String content = "Restored message";

    Message message =
        Message.reconstitute(
            messageId, chatId, senderId, content, CREATED_AT, MessageType.TEXT, null);

    assertEquals(messageId, message.getId());
    assertEquals(chatId, message.getChatId());
    assertEquals(senderId, message.getSenderId());
    assertEquals(content, message.getContent());
    assertEquals(CREATED_AT, message.getCreatedAt());
    assertEquals(MessageType.TEXT, message.getType());
    assertNull(message.getAttachment());
  }

  @Test
  void reconstitute_shouldRestoreFileMessage_whenDataIsValid() {
    Message message =
        Message.reconstitute(
            messageId, chatId, senderId, null, CREATED_AT, MessageType.FILE, attachment);

    assertEquals(messageId, message.getId());
    assertEquals(chatId, message.getChatId());
    assertEquals(senderId, message.getSenderId());
    assertNull(message.getContent());
    assertEquals(CREATED_AT, message.getCreatedAt());
    assertEquals(MessageType.FILE, message.getType());
    assertSame(attachment, message.getAttachment());
  }

  @Test
  void reconstitute_shouldThrowNullPointerException_whenIdIsNull() {
    assertThrows(
        NullPointerException.class,
        () ->
            Message.reconstitute(
                null, chatId, senderId, "Hello", CREATED_AT, MessageType.TEXT, null));
  }

  @Test
  void reconstitute_shouldThrowNullPointerException_whenChatIdIsNull() {
    assertThrows(
        NullPointerException.class,
        () ->
            Message.reconstitute(
                messageId, null, senderId, "Hello", CREATED_AT, MessageType.TEXT, null));
  }

  @Test
  void reconstitute_shouldThrowNullPointerException_whenSenderIdIsNull() {
    assertThrows(
        NullPointerException.class,
        () ->
            Message.reconstitute(
                messageId, chatId, null, "Hello", CREATED_AT, MessageType.TEXT, null));
  }

  @Test
  void reconstitute_shouldThrowNullPointerException_whenCreatedAtIsNull() {
    assertThrows(
        NullPointerException.class,
        () ->
            Message.reconstitute(
                messageId, chatId, senderId, "Hello", null, MessageType.TEXT, null));
  }

  @Test
  void reconstitute_shouldThrowNullPointerException_whenTypeIsNull() {
    assertThrows(
        NullPointerException.class,
        () -> Message.reconstitute(messageId, chatId, senderId, "Hello", CREATED_AT, null, null));
  }

  @Test
  void reconstitute_shouldThrowMessageException_whenTextContentIsBlank() {
    assertThrows(
        MessageException.class,
        () ->
            Message.reconstitute(
                messageId, chatId, senderId, " ", CREATED_AT, MessageType.TEXT, null));
  }

  @Test
  void reconstitute_shouldThrowMessageException_whenTextContentIsTooLong() {
    String content = "a".repeat(MessageConstants.MAX_CONTENT_LENGTH + 1);

    assertThrows(
        MessageException.class,
        () ->
            Message.reconstitute(
                messageId, chatId, senderId, content, CREATED_AT, MessageType.TEXT, null));
  }

  @Test
  void reconstitute_shouldThrowMessageException_whenTextMessageHasAttachment() {
    assertThrows(
        MessageException.class,
        () ->
            Message.reconstitute(
                messageId, chatId, senderId, "Hello", CREATED_AT, MessageType.TEXT, attachment));
  }

  @Test
  void reconstitute_shouldThrowMessageException_whenFileMessageHasContent() {
    assertThrows(
        MessageException.class,
        () ->
            Message.reconstitute(
                messageId,
                chatId,
                senderId,
                "Unexpected content",
                CREATED_AT,
                MessageType.FILE,
                attachment));
  }

  @Test
  void reconstitute_shouldThrowMessageException_whenTextContentIsNull() {
    assertThrows(
        MessageException.class,
        () ->
            Message.reconstitute(
                messageId, chatId, senderId, null, CREATED_AT, MessageType.TEXT, null));
  }

  @Test
  void reconstitute_shouldRestoreTextMessage_whenContentIsValid() {
    Message message =
        Message.reconstitute(
            messageId, chatId, senderId, "Hello", CREATED_AT, MessageType.TEXT, null);

    assertEquals("Hello", message.getContent());
  }

  @Test
  void reconstitute_shouldThrowMessageException_whenFileMessageHasNoAttachment() {
    assertThrows(
        MessageException.class,
        () ->
            Message.reconstitute(
                messageId, chatId, senderId, null, CREATED_AT, MessageType.FILE, null));
  }

  @Test
  void equals_shouldReturnTrue_whenMessagesAreTheSameInstance() {
    Message message = Message.ofText(messageId, chatId, senderId, "Hello");

    assertEquals(message, message);
  }

  @Test
  void equals_shouldReturnTrue_whenMessagesHaveSameId() {
    Message first = Message.ofText(messageId, chatId, senderId, "First");

    Message second =
        Message.reconstitute(
            messageId, chatId, senderId, "Second", CREATED_AT, MessageType.TEXT, null);

    assertEquals(first, second);
  }

  @Test
  void equals_shouldReturnFalse_whenMessagesHaveDifferentIds() {
    Message first = Message.ofText(messageId, chatId, senderId, "Hello");

    Message second = Message.ofText(new MessageId(ANOTHER_MESSAGE_UUID), chatId, senderId, "Hello");

    assertNotEquals(first, second);
  }

  @Test
  void equals_shouldReturnFalse_whenComparedWithNull() {
    Message message = Message.ofText(messageId, chatId, senderId, "Hello");

    assertNotEquals(null, message);
  }

  @Test
  void equals_shouldReturnFalse_whenComparedWithDifferentType() {
    Message message = Message.ofText(messageId, chatId, senderId, "Hello");

    assertNotEquals("Hello", message);
  }

  @Test
  void equals_shouldReturnFalse_whenComparingWithDifferentType() {
    Message message = Message.ofText(messageId, chatId, senderId, "Hello");

    assertThat(message.equals("user")).isFalse();
  }

  @Test
  void hashCode_shouldBeEqual_whenMessagesHaveSameId() {
    Message first = Message.ofText(messageId, chatId, senderId, "First");

    Message second =
        Message.reconstitute(
            messageId, chatId, senderId, "Second", CREATED_AT, MessageType.TEXT, null);

    assertEquals(first.hashCode(), second.hashCode());
  }
}
