package com.jeannimi.messenger.adapter.in.web.chat.dto;

import java.time.Instant;
import java.util.List;

public record ChatDto(
    Long id,
    String name,
    String type,
    List<ChatMemberDto> members,
    Instant createdAt,
    Instant lastMessageAt) {}
