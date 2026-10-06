package com.jeannimi.messenger.application.exception;

public class ChatNotFoundException extends RuntimeException {

  public ChatNotFoundException(String message) {
    super(message);
  }
}
