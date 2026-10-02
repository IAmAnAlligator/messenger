package com.jeannimi.messenger.adapter.out.websocket.dto;

import com.jeannimi.messenger.application.event.MessageDeletedEvent;
import java.time.Instant;

public record MessageDeletedWebSocketDto(
    String messageId, String chatId, String deletedBy, Instant deletedAt) {

  public static MessageDeletedWebSocketDto from(MessageDeletedEvent event) {

    return new MessageDeletedWebSocketDto(
        event.messageId().value().toString(),
        event.chatId().value().toString(),
        event.deletedBy().value().toString(),
        event.deletedAt());
  }
}
