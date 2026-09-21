package com.jeannimi.messenger.domain.message;

import com.jeannimi.messenger.domain.exception.MessageError;
import com.jeannimi.messenger.domain.exception.MessageException;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;

@Getter
public final class Message {

  private final Long id;
  private final Long chatId;
  private final Long senderId;

  private final String content;
  private final Instant createdAt;
  private final MessageType type;
  private final FileAttachment attachment;

  private Message(
      Long id,
      Long chatId,
      Long senderId,
      String content,
      Instant createdAt,
      MessageType type,
      FileAttachment attachment) {

    this.id = id;
    this.chatId = Objects.requireNonNull(chatId, "chatId");
    this.senderId = Objects.requireNonNull(senderId, "senderId");
    this.content = content;
    this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    this.type = Objects.requireNonNull(type, "type");
    this.attachment = attachment;
  }

  public static Message ofText(Long chatId, Long senderId, String content) {

    validateTextContent(content);

    String normalizedContent = content.trim();

    return new Message(
        null,
        chatId,
        senderId,
        normalizedContent,
        Instant.now(),
        MessageType.TEXT,
        null);
  }

  public static Message ofFile(Long chatId, Long senderId, FileAttachment attachment) {

    validateFileAttachment(attachment);

    return new Message(
        null,
        chatId,
        senderId,
        null,
        Instant.now(),
        MessageType.FILE,
        attachment);
  }

  public static Message reconstitute(
      Long id,
      Long chatId,
      Long senderId,
      String content,
      Instant createdAt,
      MessageType type,
      FileAttachment attachment) {

    validate(type, content, attachment);

    return new Message(
        Objects.requireNonNull(id, "id"),
        chatId,
        senderId,
        content,
        createdAt,
        type,
        attachment);
  }

  private static void validate(
      MessageType type,
      String content,
      FileAttachment attachment) {

    Objects.requireNonNull(type, "type");

    switch (type) {
      case TEXT -> {
        validateTextContent(content);
        validateTextAttachment(attachment);
      }
      case FILE -> {
        validateFileContent(content);
        validateFileAttachment(attachment);
      }
    }
  }

  private static void validateTextContent(String content) {

    if (content == null || content.isBlank()) {
      throw new MessageException(
          MessageError.CONTENT_BLANK,
          "Text message content must not be blank");
    }

    if (content.length() > MessageConstants.MAX_CONTENT_LENGTH) {
      throw new MessageException(
          MessageError.CONTENT_TOO_LONG,
          "Message content exceeds "
              + MessageConstants.MAX_CONTENT_LENGTH
              + " characters");
    }
  }

  private static void validateTextAttachment(FileAttachment attachment) {

    if (attachment != null) {
      throw new MessageException(
          MessageError.CONTENT_NOT_ALLOWED,
          "Text message must not have an attachment");
    }
  }

  private static void validateFileContent(String content) {
    if (content != null) {
      throw new MessageException(
          MessageError.CONTENT_NOT_ALLOWED,
          "File message must not have text content");
    }
  }

  private static void validateFileAttachment(FileAttachment attachment) {
    if (attachment == null) {
      throw new MessageException(
          MessageError.FILE_EMPTY,
          "File message must have an attachment");
    }
  }

  @Override
  public boolean equals(Object o) {

    if (this == o) {
      return true;
    }

    if (!(o instanceof Message that)) {
      return false;
    }

    return id != null && id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
