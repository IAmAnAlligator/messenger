package com.jeannimi.messenger.adapter.in.web.message.dto;

import com.jeannimi.messenger.application.message.dto.MessageResult;
import com.jeannimi.messenger.adapter.in.web.user.dto.UserDto;
import java.time.Instant;

public record MessageDto(
    Long id,
    Long chatId,
    UserDto sender,
    String content,
    Instant createdAt,
    FileAttachmentDto attachment) {

  public static MessageDto fromResult(MessageResult result) {
    return new MessageDto(
        result.id(),
        result.chatId(),
        UserDto.fromResult(result.sender()),
        result.content(),
        result.createdAt(),
        result.attachment() == null
            ? null
            : FileAttachmentDto.fromResult(result.attachment(), result.chatId(), result.id()));
  }
}
