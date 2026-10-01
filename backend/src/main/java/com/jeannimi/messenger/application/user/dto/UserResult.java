package com.jeannimi.messenger.application.user.dto;

import com.jeannimi.messenger.domain.user.Role;
import com.jeannimi.messenger.domain.user.UserId;
import java.util.UUID;

public record UserResult(UserId id, String handle, String username, Role role) {}
