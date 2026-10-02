package com.jeannimi.messenger.adapter.out.websocket.dto;

import com.jeannimi.messenger.application.event.ChatMemberAddedEvent;

public record ChatMemberAddedWebSocketDto(String chatId, String userId, String username) {

  public static ChatMemberAddedWebSocketDto from(ChatMemberAddedEvent event) {

    return new ChatMemberAddedWebSocketDto(
        event.chatId().value().toString(), event.userId().value().toString(), event.username());
  }
}
