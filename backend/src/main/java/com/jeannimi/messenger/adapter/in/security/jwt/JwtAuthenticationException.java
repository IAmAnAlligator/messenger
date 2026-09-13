package com.jeannimi.messenger.adapter.in.security.jwt;

public class JwtAuthenticationException extends RuntimeException {

  public JwtAuthenticationException(String message) {
    super(message);
  }

  public JwtAuthenticationException(String message, Throwable cause) {
    super(message, cause);
  }
}
