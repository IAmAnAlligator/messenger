package com.jeannimi.messenger.exception_handling;

public class ConflictException extends RuntimeException {
  public ConflictException(String message) {
    super(message);
  }
}
