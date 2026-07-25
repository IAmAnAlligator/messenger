package com.jeannimi.messenger.common.exception_handling;

public class ConflictException extends RuntimeException {
  public ConflictException(String message) {
    super(message);
  }
}
