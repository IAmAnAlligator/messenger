package com.jeannimi.messenger.domain.message;

import com.jeannimi.messenger.domain.chat.ChatId;
import com.jeannimi.messenger.domain.exception.MessageError;
import com.jeannimi.messenger.domain.exception.MessageException;
import com.jeannimi.messenger.domain.user.UserId;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;

@Getter
public final class Message {
  private final MessageId id;
  private final ChatId chatId;
  private final UserId senderId;
  private final String content;
  private final Instant createdAt;
  private final MessageType type;
  private final FileAttachment attachment;

  private Message(
      MessageId id,
      ChatId chatId,
      UserId senderId,
      String content,
      Instant createdAt,
      MessageType type,
      FileAttachment attachment) {
    this.id = Objects.requireNonNull(id, "id");
    this.chatId = Objects.requireNonNull(chatId, "chatId");
    this.senderId = Objects.requireNonNull(senderId, "senderId");
    this.content = content;
    this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    this.type = Objects.requireNonNull(type, "type");
    this.attachment = attachment;
  }

  public static Message ofText(
      MessageId messageId, ChatId chatId, UserId senderId, String content) {
    Objects.requireNonNull(messageId, "messageId");
    String normalizedContent = normalizeContent(content);
    validateTextContent(normalizedContent);
    return new Message(
        messageId, chatId, senderId, normalizedContent, Instant.now(), MessageType.TEXT, null);
  }

  public static Message ofFile(
      MessageId messageId, ChatId chatId, UserId senderId, FileAttachment attachment) {
    Objects.requireNonNull(messageId, "messageId");
    validateFileAttachment(attachment);
    return new Message(
        messageId, chatId, senderId, null, Instant.now(), MessageType.FILE, attachment);
  }

  public static Message reconstitute(
      MessageId id,
      ChatId chatId,
      UserId senderId,
      String content,
      Instant createdAt,
      MessageType type,
      FileAttachment attachment) {
    Objects.requireNonNull(id, "id");
    Objects.requireNonNull(chatId, "chatId");
    Objects.requireNonNull(senderId, "senderId");
    validate(type, content, attachment);
    return new Message(id, chatId, senderId, content, createdAt, type, attachment);
  }

  private static void validate(MessageType type, String content, FileAttachment attachment) {
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

  private static String normalizeContent(String content) {
    return Objects.requireNonNull(content, "content").trim();
  }

  private static void validateTextContent(String content) {
    if (content == null || content.isBlank()) {
      throw new MessageException(
          MessageError.CONTENT_BLANK, "Text message content must not be blank");
    }
    if (content.length() > MessageConstants.MAX_CONTENT_LENGTH) {
      throw new MessageException(
          MessageError.CONTENT_TOO_LONG,
          "Message content exceeds " + MessageConstants.MAX_CONTENT_LENGTH + " characters");
    }
  }

  private static void validateTextAttachment(FileAttachment attachment) {
    if (attachment != null) {
      throw new MessageException(
          MessageError.CONTENT_NOT_ALLOWED, "Text message must not have an attachment");
    }
  }

  private static void validateFileContent(String content) {
    if (content != null) {
      throw new MessageException(
          MessageError.CONTENT_NOT_ALLOWED, "File message must not have text content");
    }
  }

  private static void validateFileAttachment(FileAttachment attachment) {
    if (attachment == null) {
      throw new MessageException(MessageError.FILE_EMPTY, "File message must have an attachment");
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
    return id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return id.hashCode();
  }
}
