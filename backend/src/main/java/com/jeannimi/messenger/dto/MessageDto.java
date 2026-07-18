package com.jeannimi.messenger.dto;

import com.jeannimi.messenger.entity.Message;
import com.jeannimi.messenger.entity.MessageStatus;
import java.time.Instant;

public record MessageDto(
    Long id, Long chatId, UserDto sender, String content, Instant createdAt, MessageStatus status) {

  public static MessageDto toDto(Message message) {

    return new MessageDto(
        message.getId(),
        message.getChat().getId(),
        UserDto.toDto(message.getSender()),
        message.getContent(),
        message.getCreatedAt(),
        message.getStatus());
  }
}
