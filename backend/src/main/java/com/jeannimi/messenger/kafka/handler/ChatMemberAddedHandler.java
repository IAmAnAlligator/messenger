package com.jeannimi.messenger.kafka.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeannimi.messenger.kafka.event.ChatMemberAddedEvent;
import com.jeannimi.messenger.kafka.event.EventType;
import com.jeannimi.messenger.kafka.event.WebSocketEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatMemberAddedHandler implements ChatEventHandler {

  private final ObjectMapper objectMapper;

  private final SimpMessagingTemplate messagingTemplate;

  @Override
  public EventType supports() {

    return EventType.CHAT_MEMBER_ADDED;
  }

  @Override
  public void handle(JsonNode payload) {

    try {

      ChatMemberAddedEvent dto = objectMapper.treeToValue(payload, ChatMemberAddedEvent.class);

      WebSocketEvent<ChatMemberAddedEvent> event =
          WebSocketEvent.of(EventType.CHAT_MEMBER_ADDED, dto);

      messagingTemplate.convertAndSend("/topic/chat/" + dto.chatId(), event);

      /*
         Отдельно уведомляем
         нового пользователя,
         чтобы обновить список чатов
      */

      messagingTemplate.convertAndSend("/topic/user/" + dto.userId() + "/chats", event);

    } catch (Exception e) {

      throw new RuntimeException("Failed to process CHAT_MEMBER_ADDED", e);
    }
  }
}
