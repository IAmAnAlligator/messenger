package com.jeannimi.messenger.domain.message;

import com.jeannimi.messenger.common.exception_handling.MessageError;
import com.jeannimi.messenger.common.exception_handling.MessageException;
import com.jeannimi.messenger.message.MessageConstants;
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

    Objects.requireNonNull(chatId, "chatId");
    Objects.requireNonNull(senderId, "senderId");

    if (content == null || content.isBlank()) {
      throw new MessageException(MessageError.CONTENT_BLANK, "Message content must not be blank");
    }

    content = content.trim();

    if (content.length() > MessageConstants.MAX_CONTENT_LENGTH) {
      throw new MessageException(
          MessageError.CONTENT_TOO_LONG,
          "Message content exceeds " + MessageConstants.MAX_CONTENT_LENGTH + " characters");
    }

    return new Message(null, chatId, senderId, content, Instant.now(), MessageType.TEXT, null);
  }

  public static Message ofFile(Long chatId, Long senderId, FileAttachment attachment) {

    Objects.requireNonNull(chatId, "chatId");
    Objects.requireNonNull(senderId, "senderId");
    Objects.requireNonNull(attachment, "attachment");

    return new Message(null, chatId, senderId, null, Instant.now(), MessageType.FILE, attachment);
  }

  public static Message reconstitute(
      Long id,
      Long chatId,
      Long senderId,
      String content,
      Instant createdAt,
      MessageType type,
      FileAttachment attachment) {

    return new Message(
        Objects.requireNonNull(id, "id"), chatId, senderId, content, createdAt, type, attachment);
  }

  public boolean isText() {
    return type == MessageType.TEXT;
  }

  public boolean isFile() {
    return type == MessageType.FILE;
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
