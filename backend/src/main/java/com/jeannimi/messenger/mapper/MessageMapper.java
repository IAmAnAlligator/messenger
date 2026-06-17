package com.jeannimi.messenger.mapper;

import com.jeannimi.messenger.dto.MessageDto;
import com.jeannimi.messenger.dto.UserDto;
import com.jeannimi.messenger.entity.Message;
import lombok.experimental.UtilityClass;

@UtilityClass
public class MessageMapper {

  public MessageDto toDto(Message message) {
    return MessageDto.builder()
        .id(message.getId())
        .chatId(message.getChat().getId())
        .content(message.getContent())
        .createdAt(message.getCreatedAt())
        .status(message.getStatus().name())
        .sender(
            UserDto.builder()
                .id(message.getSender().getId())
                .username(message.getSender().getUsername())
                .build())
        .build();
  }
}
