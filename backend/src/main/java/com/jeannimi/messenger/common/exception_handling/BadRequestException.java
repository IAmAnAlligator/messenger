package com.jeannimi.messenger.common.exception_handling;

public class BadRequestException extends RuntimeException {
  public BadRequestException(String message) {
    super(message);
  }
}
