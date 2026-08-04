package com.jeannimi.messenger.kafka.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeannimi.messenger.kafka.event.ChatMemberLeftEvent;
import com.jeannimi.messenger.kafka.event.EventType;
import com.jeannimi.messenger.kafka.event.WebSocketEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatMemberLeftHandler implements ChatEventHandler {

  private final ObjectMapper objectMapper;
  private final SimpMessagingTemplate messagingTemplate;

  @Override
  public EventType supports() {
    return EventType.CHAT_MEMBER_LEFT;
  }

  @Override
  public void handle(JsonNode payload) {

    try {

      ChatMemberLeftEvent dto = objectMapper.treeToValue(payload, ChatMemberLeftEvent.class);

      WebSocketEvent<ChatMemberLeftEvent> event =
          WebSocketEvent.of(EventType.CHAT_MEMBER_LEFT, dto);

      messagingTemplate.convertAndSend("/topic/chat/" + dto.chatId(), event);

      messagingTemplate.convertAndSend("/topic/user/" + dto.userId() + "/chats", event);

    } catch (Exception e) {

      throw new RuntimeException("Failed to process CHAT_MEMBER_LEFT", e);
    }
  }
}
