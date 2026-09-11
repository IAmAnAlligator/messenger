package com.jeannimi.messenger.adapter.in.kafka.handler;

import com.fasterxml.jackson.databind.JsonNode;
import com.jeannimi.messenger.adapter.kafka.event.EventType;

public interface ChatEventHandler {

  EventType supports();

  void handle(JsonNode payload);
}
