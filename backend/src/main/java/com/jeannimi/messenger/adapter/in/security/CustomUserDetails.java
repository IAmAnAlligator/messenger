package com.jeannimi.messenger.adapter.in.security;

import com.jeannimi.messenger.domain.user.UserId;

public record CustomUserDetails(UserId id, String role) {}
