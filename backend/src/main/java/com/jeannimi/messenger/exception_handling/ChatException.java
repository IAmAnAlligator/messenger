package com.jeannimi.messenger.exception_handling;

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
