package com.jeannimi.messenger.kafka.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.jeannimi.messenger.kafka.event.EventType;

public interface ChatEventHandler {

  EventType supports();

  void handle(JsonNode payload);
}
