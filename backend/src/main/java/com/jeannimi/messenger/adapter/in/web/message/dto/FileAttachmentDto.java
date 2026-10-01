package com.jeannimi.messenger.adapter.in.web.message.dto;

import com.jeannimi.messenger.application.message.dto.FileAttachmentResult;
import com.jeannimi.messenger.domain.chat.ChatId;
import com.jeannimi.messenger.domain.message.MessageId;
import java.util.UUID;

public record FileAttachmentDto(
    UUID id, String originalFileName, String contentType, long size, String url) {

  public static FileAttachmentDto fromResult(
      FileAttachmentResult result, ChatId chatId, MessageId messageId) {
    return new FileAttachmentDto(
        result.id().value(),
        result.originalFileName(),
        result.contentType(),
        result.size(),
        buildUrl(chatId, messageId));
  }

  private static String buildUrl(ChatId chatId, MessageId messageId) {
    return "/api/chats/" + chatId + "/messages/" + messageId + "/file";
  }
}
