package com.jeannimi.messenger.adapter.out.websocket.dto;

import com.jeannimi.messenger.application.event.MessageReadEvent;
import java.time.Instant;

public record MessageReadWebSocketDto(
    String messageId, String chatId, String readerId, Instant readAt, String lastReadMessageId) {

  public static MessageReadWebSocketDto from(MessageReadEvent event) {

    return new MessageReadWebSocketDto(
        event.messageId().value().toString(),
        event.chatId().value().toString(),
        event.readerId().value().toString(),
        event.readAt(),
        event.lastReadMessageId().value().toString());
  }
}
