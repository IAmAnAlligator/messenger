package com.jeannimi.messenger.adapter.out.websocket.dto;

import com.jeannimi.messenger.application.user.dto.UserResult;

public record UserWebSocketDto(String id, String handle, String username, String role) {

  public static UserWebSocketDto from(UserResult user) {
    return new UserWebSocketDto(
        user.id().value().toString(), user.handle(), user.username(), user.role().name());
  }
}
