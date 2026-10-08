package com.jeannimi.messenger.adapter.in.web.chat.dto;

import com.jeannimi.messenger.adapter.in.web.user.dto.UserDto;
import java.time.Instant;
import java.util.UUID;

public record LastMessageDto(
    UUID id,
    UserDto sender,
    String content,
    Instant createdAt) {}