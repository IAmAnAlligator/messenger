package com.jeannimi.messenger.message.dto;

import com.jeannimi.messenger.message.entity.FileAttachment;
import com.jeannimi.messenger.message.entity.Message;
import com.jeannimi.messenger.user.dto.UserDto;
import java.time.Instant;

public record MessageDto(
    Long id,
    Long chatId,
    UserDto sender,
    String content,
    Instant createdAt,
    FileAttachmentDto attachment) {

  public static MessageDto toDto(Message message) {

    FileAttachment attachment = message.getAttachment();

    FileAttachmentDto attachmentDto =
        attachment == null
            ? null
            : FileAttachmentDto.toDto(attachment, message.getChat().getId(), message.getId());

    return new MessageDto(
        message.getId(),
        message.getChat().getId(),
        UserDto.toDto(message.getSender()),
        message.getContent(),
        message.getCreatedAt(),
        attachmentDto);
  }
}
