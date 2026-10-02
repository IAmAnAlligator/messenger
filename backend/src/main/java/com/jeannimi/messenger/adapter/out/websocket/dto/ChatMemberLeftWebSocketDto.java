package com.jeannimi.messenger.adapter.out.websocket.dto;

import com.jeannimi.messenger.application.event.ChatMemberLeftEvent;

public record ChatMemberLeftWebSocketDto(String chatId, String userId) {

  public static ChatMemberLeftWebSocketDto from(ChatMemberLeftEvent event) {

    return new ChatMemberLeftWebSocketDto(
        event.chatId().value().toString(), event.userId().value().toString());
  }
}
