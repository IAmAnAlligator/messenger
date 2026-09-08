package com.jeannimi.messenger.user.dto;

import com.jeannimi.messenger.application.user.dto.UserResult;
import com.jeannimi.messenger.domain.user.Role;

public record UserDto(Long id, String username, Role role) {

  public static UserDto fromResult(UserResult result) {
    return new UserDto(result.id(), result.username(), result.role());
  }
}
