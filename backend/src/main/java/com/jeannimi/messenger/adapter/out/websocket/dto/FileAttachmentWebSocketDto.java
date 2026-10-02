package com.jeannimi.messenger.adapter.out.websocket.dto;

import com.jeannimi.messenger.application.message.dto.FileAttachmentResult;

public record FileAttachmentWebSocketDto(
    String id, String originalFileName, String contentType, long size) {

  public static FileAttachmentWebSocketDto from(FileAttachmentResult attachment) {

    if (attachment == null) {
      return null;
    }

    return new FileAttachmentWebSocketDto(
        attachment.id().value().toString(),
        attachment.originalFileName(),
        attachment.contentType(),
        attachment.size());
  }
}
