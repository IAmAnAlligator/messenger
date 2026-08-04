package com.jeannimi.messenger.kafka.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeannimi.messenger.kafka.event.ChatCreatedEvent;
import com.jeannimi.messenger.kafka.event.EventType;
import com.jeannimi.messenger.kafka.event.WebSocketEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatCreatedHandler implements ChatEventHandler {

  private final ObjectMapper objectMapper;

  private final SimpMessagingTemplate messagingTemplate;

  @Override
  public EventType supports() {

    return EventType.CHAT_CREATED;
  }

  @Override
  public void handle(JsonNode payload) {

    try {

      ChatCreatedEvent dto = objectMapper.treeToValue(payload, ChatCreatedEvent.class);

      WebSocketEvent<ChatCreatedEvent> event = WebSocketEvent.of(EventType.CHAT_CREATED, dto);

      /*
         Уведомляем подписчиков,
         которые слушают создание чатов
      */

      messagingTemplate.convertAndSend("/topic/chat.created", event);

      /*
         Обновляем список чатов
         у каждого пользователя
      */

      for (Long memberId : dto.memberIds()) {

        messagingTemplate.convertAndSend("/topic/user/" + memberId + "/chats", event);
      }

    } catch (Exception e) {

      throw new RuntimeException("Failed to process CHAT_CREATED", e);
    }
  }
}
