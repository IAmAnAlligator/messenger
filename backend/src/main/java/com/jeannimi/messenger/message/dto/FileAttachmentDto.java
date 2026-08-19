package com.jeannimi.messenger.message.dto;

import com.jeannimi.messenger.message.entity.FileAttachment;
import java.util.UUID;

public record FileAttachmentDto(
    UUID id, String originalFileName, String contentType, long size, String url) {

  public static FileAttachmentDto toDto(FileAttachment attachment, Long chatId, Long messageId) {

    return new FileAttachmentDto(
        attachment.getId(),
        attachment.getOriginalFileName(),
        attachment.getContentType(),
        attachment.getSize(),
        "/api/chats/" + chatId + "/messages/" + messageId + "/file");
  }
}
