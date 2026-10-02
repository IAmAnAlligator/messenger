package com.jeannimi.messenger.adapter.out.websocket.dto;

import com.jeannimi.messenger.application.event.ChatRenamedEvent;

public record ChatRenamedWebSocketDto(String chatId, String oldName, String newName) {

  public static ChatRenamedWebSocketDto from(ChatRenamedEvent event) {

    return new ChatRenamedWebSocketDto(
        event.chatId().value().toString(), event.oldName(), event.newName());
  }
}
