package com.jeannimi.messenger.kafka.event;

public record WebSocketEvent<T>(
    EventType type,
    T payload
) {

  public static <T> WebSocketEvent<T> of(
      EventType type,
      T payload) {

    return new WebSocketEvent<>(type, payload);
  }

}
