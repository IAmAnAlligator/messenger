package com.jeannimi.messenger.adapter.in.kafka.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeannimi.messenger.adapter.kafka.event.ChatRenamedEvent;
import com.jeannimi.messenger.adapter.kafka.event.EventType;
import com.jeannimi.messenger.adapter.in.websocket.WebSocketEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatRenamedHandler implements ChatEventHandler {

  private final ObjectMapper objectMapper;
  private final SimpMessagingTemplate messagingTemplate;

  @Override
  public EventType supports() {
    return EventType.CHAT_RENAMED;
  }

  @Override
  public void handle(JsonNode payload) {

    try {

      ChatRenamedEvent chatRenamedEvent = objectMapper.treeToValue(payload, ChatRenamedEvent.class);

      WebSocketEvent<ChatRenamedEvent> event =
          WebSocketEvent.of(EventType.CHAT_RENAMED, chatRenamedEvent);

      messagingTemplate.convertAndSend("/topic/chat/" + chatRenamedEvent.chatId(), event);

    } catch (Exception e) {

      throw new RuntimeException("Failed to process CHAT_RENAMED", e);
    }
  }
}
