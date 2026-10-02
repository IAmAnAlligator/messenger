package com.jeannimi.messenger.adapter.out.websocket.dto;

import com.jeannimi.messenger.application.event.ChatCreatedEvent;
import java.util.List;

public record ChatCreatedWebSocketDto(
    String chatId, String name, String type, List<String> memberIds) {

  public static ChatCreatedWebSocketDto from(ChatCreatedEvent event) {

    return new ChatCreatedWebSocketDto(
        event.chatId().value().toString(),
        event.name(),
        event.type().name(),
        event.memberIds().stream().map(id -> id.value().toString()).toList());
  }
}
