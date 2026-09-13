package com.jeannimi.messenger.domain.exception;

public class MessageException extends RuntimeException {

  private final MessageError error;

  public MessageException(MessageError error, String message) {
    super(message);
    this.error = error;
  }

  public MessageError getError() {
    return error;
  }
}
