package com.jeannimi.messenger.adapter.in.kafka.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeannimi.messenger.application.event.ChatMemberAddedEvent;
import com.jeannimi.messenger.application.event.EventType;
import com.jeannimi.messenger.application.port.out.RealtimeEventPublisherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatMemberAddedHandler implements ChatEventHandler {

  private final ObjectMapper objectMapper;
  private final RealtimeEventPublisherPort realtimeEventPublisher;

  @Override
  public EventType supports() {
    return EventType.CHAT_MEMBER_ADDED;
  }

  @Override
  public void handle(JsonNode payload) {

    try {
      ChatMemberAddedEvent event =
          objectMapper.treeToValue(payload, ChatMemberAddedEvent.class);

      realtimeEventPublisher.publish(
          EventType.CHAT_MEMBER_ADDED,
          event);

    } catch (Exception e) {
      throw new RuntimeException(
          "Failed to process CHAT_MEMBER_ADDED", e);
    }
  }
}