package com.jeannimi.messenger.kafka.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeannimi.messenger.kafka.event.ChatMemberAddedEvent;
import com.jeannimi.messenger.common.constants.EventType;
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

      messagingTemplate.convertAndSend("/topic/chat/" + dto.chatId(), dto);

      /*
         Отдельно уведомляем
         нового пользователя,
         чтобы обновить список чатов
      */

      messagingTemplate.convertAndSend("/topic/user/" + dto.userId() + "/chats", dto);

    } catch (Exception e) {

      throw new RuntimeException("Failed to process CHAT_MEMBER_ADDED", e);
    }
  }
}
