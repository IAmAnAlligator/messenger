package com.jeannimi.messenger.adapter.in.security.jwt;

import org.springframework.security.core.AuthenticationException;

public class JwtAuthenticationException extends AuthenticationException {

  public JwtAuthenticationException(String message) {
    super(message);
  }

  public JwtAuthenticationException(String message, Throwable cause) {
    super(message, cause);
  }
}