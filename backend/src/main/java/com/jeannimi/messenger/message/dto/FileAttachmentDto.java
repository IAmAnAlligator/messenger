package com.jeannimi.messenger.message.dto;

import com.jeannimi.messenger.application.message.dto.FileAttachmentResult;
import java.util.UUID;

public record FileAttachmentDto(
    UUID id, String originalFileName, String contentType, long size, String url) {

  public static FileAttachmentDto fromResult(FileAttachmentResult result,
      Long chatId, Long messageId) {
    return new FileAttachmentDto(
        result.id(),
        result.originalFileName(),
        result.contentType(),
        result.size(),
        buildUrl(chatId, messageId));
  }

  private static String buildUrl(Long chatId, Long messageId) {
    return "/api/chats/" + chatId + "/messages/" + messageId + "/file";
  }
}
