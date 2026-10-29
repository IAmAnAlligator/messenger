package com.jeannimi.messenger.adapter.in.kafka.exception;

public class InvalidKafkaEventException extends RuntimeException {

  public InvalidKafkaEventException(String message) {
    super(message);
  }

  public InvalidKafkaEventException(String message, Throwable cause) {
    super(message, cause);
  }
}
