package com.jeannimi.messenger.adapter.in.kafka.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeannimi.messenger.application.event.ChatMemberLeftEvent;
import com.jeannimi.messenger.application.event.EventType;
import com.jeannimi.messenger.application.port.out.RealtimeEventPublisherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatMemberLeftHandler implements ChatEventHandler {

  private final ObjectMapper objectMapper;
  private final RealtimeEventPublisherPort realtimeEventPublisher;

  @Override
  public EventType supports() {
    return EventType.CHAT_MEMBER_LEFT;
  }

  @Override
  public void handle(JsonNode payload) {

    try {
      ChatMemberLeftEvent kafkaEvent =
          objectMapper.treeToValue(payload, ChatMemberLeftEvent.class);

      com.jeannimi.messenger.application.event.ChatMemberLeftEvent applicationEvent =
          new com.jeannimi.messenger.application.event.ChatMemberLeftEvent(
              kafkaEvent.chatId(),
              kafkaEvent.userId());

      realtimeEventPublisher.publish(
          EventType.CHAT_MEMBER_LEFT,
          applicationEvent);

    } catch (Exception e) {
      throw new RuntimeException(
          "Failed to process CHAT_MEMBER_LEFT", e);
    }
  }
}