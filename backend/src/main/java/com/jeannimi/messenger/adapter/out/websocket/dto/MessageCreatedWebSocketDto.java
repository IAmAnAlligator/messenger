package com.jeannimi.messenger.adapter.out.websocket.dto;

import com.jeannimi.messenger.application.event.MessageCreatedEvent;
import java.time.Instant;

public record MessageCreatedWebSocketDto(
    String id,
    String chatId,
    UserWebSocketDto sender,
    String content,
    Instant createdAt,
    FileAttachmentWebSocketDto attachment) {

  public static MessageCreatedWebSocketDto from(MessageCreatedEvent event) {

    var message = event.message();

    return new MessageCreatedWebSocketDto(
        message.id().value().toString(),
        message.chatId().value().toString(),
        UserWebSocketDto.from(message.sender()),
        message.content(),
        message.createdAt(),
        FileAttachmentWebSocketDto.from(message.attachment()));
  }
}
