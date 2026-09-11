package com.jeannimi.messenger.adapter.in.kafka.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeannimi.messenger.adapter.kafka.event.ChatDeletedEvent;
import com.jeannimi.messenger.adapter.kafka.event.EventType;
import com.jeannimi.messenger.adapter.in.websocket.WebSocketEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatDeletedHandler implements ChatEventHandler {

  private final ObjectMapper objectMapper;

  private final SimpMessagingTemplate messagingTemplate;

  @Override
  public EventType supports() {

    return EventType.CHAT_DELETED;
  }

  @Override
  public void handle(JsonNode payload) {

    try {

      ChatDeletedEvent chatDeletedEvent = objectMapper.treeToValue(payload, ChatDeletedEvent.class);

      WebSocketEvent<ChatDeletedEvent> event =
          WebSocketEvent.of(EventType.CHAT_DELETED, chatDeletedEvent);

      /*
         Сообщаем участникам,
         что чат удален
      */

      messagingTemplate.convertAndSend("/topic/chat/" + chatDeletedEvent.chatId(), event);

      /*
         Глобальное событие удаления
      */

      messagingTemplate.convertAndSend("/topic/chat.deleted", event);

    } catch (Exception e) {

      throw new RuntimeException("Failed to process CHAT_DELETED", e);
    }
  }
}
