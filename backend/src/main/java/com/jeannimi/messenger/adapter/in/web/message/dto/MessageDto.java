package com.jeannimi.messenger.adapter.in.web.message.dto;

import com.jeannimi.messenger.adapter.in.web.user.dto.UserDto;
import com.jeannimi.messenger.application.message.dto.MessageResult;
import java.time.Instant;
import java.util.UUID;

public record MessageDto(
    UUID id,
    UUID chatId,
    UserDto sender,
    String content,
    Instant createdAt,
    FileAttachmentDto attachment) {

  public static MessageDto fromResult(MessageResult result) {
    return new MessageDto(
        result.id().value(),
        result.chatId().value(),
        UserDto.fromResult(result.sender()),
        result.content(),
        result.createdAt(),
        result.attachment() == null
            ? null
            : FileAttachmentDto.fromResult(result.attachment(), result.chatId(), result.id()));
  }
}
