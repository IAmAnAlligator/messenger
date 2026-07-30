package com.jeannimi.messenger.kafka.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeannimi.messenger.kafka.event.ChatMemberLeftEvent;
import com.jeannimi.messenger.kafka.event.ChatRenamedEvent;
import com.jeannimi.messenger.kafka.event.EventType;
import com.jeannimi.messenger.kafka.event.WebSocketEvent;
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

      ChatRenamedEvent dto = objectMapper.treeToValue(payload, ChatRenamedEvent.class);

      WebSocketEvent<ChatRenamedEvent> event =
          WebSocketEvent.of(
              EventType.CHAT_RENAMED,
              dto);

      messagingTemplate.convertAndSend("/topic/chat/" + dto.chatId(), event);

    } catch (Exception e) {

      throw new RuntimeException("Failed to process CHAT_RENAMED", e);
    }
  }
}
