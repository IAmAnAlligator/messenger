package com.jeannimi.messenger.kafka.event;

import com.jeannimi.messenger.user.dto.UserDto;
import com.jeannimi.messenger.message.entity.Message;
import java.time.Instant;

public record MessageSentEvent(
    Long messageId,
    Long chatId,
    UserDto sender,
    String content,
    Instant createdAt
) {

  public static MessageSentEvent from(Message message) {

    return new MessageSentEvent(
        message.getId(),
        message.getChat().getId(),
        UserDto.toDto(message.getSender()),
        message.getContent(),
        message.getCreatedAt()
    );
  }
}
