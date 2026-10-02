package com.jeannimi.messenger.adapter.out.websocket.dto;

import com.jeannimi.messenger.application.event.ChatMemberRemovedEvent;

public record ChatMemberRemovedWebSocketDto(String chatId, String userId) {

  public static ChatMemberRemovedWebSocketDto from(ChatMemberRemovedEvent event) {

    return new ChatMemberRemovedWebSocketDto(
        event.chatId().value().toString(), event.userId().value().toString());
  }
}
