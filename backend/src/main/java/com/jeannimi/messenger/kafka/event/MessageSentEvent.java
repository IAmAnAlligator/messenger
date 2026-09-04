package com.jeannimi.messenger.kafka.event;

import com.jeannimi.messenger.application.message.dto.FileAttachmentResult;
import com.jeannimi.messenger.application.message.dto.MessageResult;
import com.jeannimi.messenger.application.user.dto.UserResult;
import java.time.Instant;

public record MessageSentEvent(
    Long messageId,
    Long chatId,
    UserResult sender,
    String content,
    Instant createdAt,
    FileAttachmentResult attachment) {

  public static MessageSentEvent from(MessageResult result) {
    return new MessageSentEvent(
        result.id(),
        result.chatId(),
        result.sender(),
        result.content(),
        result.createdAt(),
        result.attachment());
  }

  public MessageResult toResult() {
    return new MessageResult(
        messageId,
        chatId,
        sender,
        content,
        createdAt,
        attachment);
  }
}
