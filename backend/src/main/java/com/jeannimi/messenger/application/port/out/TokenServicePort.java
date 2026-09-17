package com.jeannimi.messenger.application.port.out;

import com.jeannimi.messenger.domain.user.User;

public interface TokenServicePort {

  String generateAccessToken(User user);

  String generateRefreshToken(User user);

  boolean isTokenValid(String token);

  Long extractUserId(String token);

  String extractTokenType(String token);

  String extractRole(String token);

}