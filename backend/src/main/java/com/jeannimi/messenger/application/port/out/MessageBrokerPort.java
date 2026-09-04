package com.jeannimi.messenger.application.port.out;

public interface MessageBrokerPort {

  void publish(
      String topic,
      String key,
      String payload);
}