package com.jeannimi.messenger.common.exception_handling;

public class UnauthorizedException extends RuntimeException {
  public UnauthorizedException(String message) {
    super(message);
  }
}
