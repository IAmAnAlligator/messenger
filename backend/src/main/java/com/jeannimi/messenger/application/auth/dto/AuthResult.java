package com.jeannimi.messenger.application.auth.dto;

public record AuthResult(
    String accessToken,
    String refreshToken) {}