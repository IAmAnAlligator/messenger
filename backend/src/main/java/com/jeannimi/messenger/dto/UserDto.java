package com.jeannimi.messenger.dto;

import com.jeannimi.messenger.entity.Role;
import com.jeannimi.messenger.entity.User;

public record UserDto(Long id, String username, Role role) {

  public static UserDto toDto(User user) {
    return new UserDto(user.getId(), user.getUsername().getValue(), user.getRole());
  }
}
