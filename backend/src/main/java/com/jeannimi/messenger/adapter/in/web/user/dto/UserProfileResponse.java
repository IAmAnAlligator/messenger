package com.jeannimi.messenger.adapter.in.web.user.dto;

import java.util.UUID;

public record UserProfileResponse(UUID id, String username, String handle) {}
