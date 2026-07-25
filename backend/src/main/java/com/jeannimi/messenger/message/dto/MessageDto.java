package com.jeannimi.messenger.message.dto;

import com.jeannimi.messenger.user.dto.UserDto;
import com.jeannimi.messenger.message.entity.Message;
import com.jeannimi.messenger.message.entity.MessageStatus;
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
