package com.jeannimi.messenger.adapter.in.websocket;

import com.jeannimi.messenger.adapter.kafka.event.EventType;

public record WebSocketEvent<T>(EventType type, T payload) {

  public static <T> WebSocketEvent<T> of(EventType type, T payload) {

    return new WebSocketEvent<>(type, payload);
  }
}
