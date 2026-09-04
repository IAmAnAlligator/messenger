package com.jeannimi.messenger.application.user.dto;

import com.jeannimi.messenger.user.entity.Role;

public record UserResult(
    Long id,
    String username,
    Role role) {

}

