package com.jeannimi.messenger.application.user.dto;

import com.jeannimi.messenger.domain.user.UserId;

public record UserProfileResult(
    UserId id,
    String username,
    String handle
) {}