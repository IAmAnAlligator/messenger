package com.jeannimi.messenger.common.exception_handling;

public class OutboxException extends RuntimeException {

  public OutboxException(String message, Throwable cause) {
    super(message, cause);
  }
}
