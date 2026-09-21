package com.jeannimi.messenger.adapter.in.kafka.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jeannimi.messenger.application.event.ChatCreatedEvent;
import com.jeannimi.messenger.application.event.EventType;
import com.jeannimi.messenger.application.port.out.RealtimeEventPublisherPort;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatCreatedHandler implements ChatEventHandler {

  private final ObjectMapper objectMapper;
  private final RealtimeEventPublisherPort realtimeEventPublisher;

  @Override
  public EventType supports() {
    return EventType.CHAT_CREATED;
  }

  @Override
  public void handle(JsonNode payload) {

    try {
      ChatCreatedEvent event = objectMapper.treeToValue(payload, ChatCreatedEvent.class);

      realtimeEventPublisher.publish(EventType.CHAT_CREATED, event);

    } catch (Exception e) {
      throw new RuntimeException("Failed to process CHAT_CREATED", e);
    }
  }
}
