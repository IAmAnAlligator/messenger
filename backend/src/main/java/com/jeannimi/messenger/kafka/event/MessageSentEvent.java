package com.jeannimi.messenger.kafka.event;

import com.jeannimi.messenger.message.dto.FileAttachmentDto;
import com.jeannimi.messenger.message.dto.MessageDto;
import com.jeannimi.messenger.message.entity.Message;
import com.jeannimi.messenger.user.dto.UserDto;
import java.time.Instant;

public record MessageSentEvent(
    Long messageId,
    Long chatId,
    UserDto sender,
    String content,
    Instant createdAt,
    FileAttachmentDto attachment) {

  public static MessageSentEvent from(Message message) {

    return new MessageSentEvent(
        message.getId(),
        message.getChat().getId(),
        UserDto.toDto(message.getSender()),
        message.getContent(),
        message.getCreatedAt(),
        MessageDto.toDto(message).attachment());
  }
}
