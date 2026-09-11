package com.jeannimi.messenger.application.port.out;

import com.jeannimi.messenger.domain.user.User;

public interface TokenServicePort {

//  String ACCESS_TOKEN_TYPE = "ACCESS";
//  String REFRESH_TOKEN_TYPE = "REFRESH";

  String generateAccessToken(User user);

  String generateRefreshToken(User user);

  boolean isTokenValid(String token);

  Long extractUserId(String token);

  String extractTokenType(String token);

}