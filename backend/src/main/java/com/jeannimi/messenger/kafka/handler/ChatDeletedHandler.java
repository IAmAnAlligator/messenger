package com.jeannimi.messenger.kafka.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeannimi.messenger.kafka.event.ChatDeletedEvent;
import com.jeannimi.messenger.common.constants.EventType;
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

      ChatDeletedEvent dto = objectMapper.treeToValue(payload, ChatDeletedEvent.class);

      /*
         Сообщаем участникам,
         что чат удален
      */

      messagingTemplate.convertAndSend("/topic/chat/" + dto.chatId(), dto);

      /*
         Глобальное событие удаления
      */

      messagingTemplate.convertAndSend("/topic/chat.deleted", dto);

    } catch (Exception e) {

      throw new RuntimeException("Failed to process CHAT_DELETED", e);
    }
  }
}
