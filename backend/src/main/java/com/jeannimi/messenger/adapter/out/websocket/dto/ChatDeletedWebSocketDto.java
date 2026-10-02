package com.jeannimi.messenger.adapter.out.websocket.dto;

import com.jeannimi.messenger.application.event.ChatDeletedEvent;

public record ChatDeletedWebSocketDto(String chatId) {

  public static ChatDeletedWebSocketDto from(ChatDeletedEvent event) {

    return new ChatDeletedWebSocketDto(event.chatId().value().toString());
  }
}
