package com.jeannimi.messenger.message.dto;

import com.jeannimi.messenger.message.entity.FileAttachment;
import com.jeannimi.messenger.message.entity.Message;
import com.jeannimi.messenger.message.entity.MessageStatus;
import com.jeannimi.messenger.user.dto.UserDto;
import java.time.Instant;

public record MessageDto(
    Long id, Long chatId, UserDto sender, String content, Instant createdAt, MessageStatus status
,FileAttachmentDto attachment) {

  public static MessageDto toDto(Message message) {

    FileAttachment attachment =
        message.getAttachment();

    FileAttachmentDto attachmentDto =
        attachment == null
            ? null
            : new FileAttachmentDto(
                attachment.getId(),
                attachment.getOriginalFileName(),
                attachment.getContentType(),
                attachment.getSize(),
                "/api/chats/"
                    + message.getChat().getId()
                    + "/messages/"
                    + message.getId()
                    + "/file");

    return new MessageDto(
        message.getId(),
        message.getChat().getId(),
        UserDto.toDto(message.getSender()),
        message.getContent(),
        message.getCreatedAt(),
        message.getStatus(),
        attachmentDto);
  }
}
