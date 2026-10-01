package com.jeannimi.messenger.application.port.out;

import com.jeannimi.messenger.domain.user.User;
import com.jeannimi.messenger.domain.user.UserId;
import java.util.UUID;

public interface TokenServicePort {

  String generateAccessToken(User user);

  String generateRefreshToken(User user);

  boolean isTokenValid(String token);

  UserId extractUserId(String token);

  String extractTokenType(String token);

  String extractRole(String token);
}
