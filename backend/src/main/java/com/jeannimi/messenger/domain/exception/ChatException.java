package com.jeannimi.messenger.domain.exception;

public class ChatException extends RuntimeException {

  private final ChatError error;

  public ChatException(ChatError error, String message) {
    super(message);
    this.error = error;
  }

  public ChatError getError() {
    return error;
  }
}
