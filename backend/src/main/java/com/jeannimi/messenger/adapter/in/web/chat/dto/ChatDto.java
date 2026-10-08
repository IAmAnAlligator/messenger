package com.jeannimi.messenger.adapter.in.web.chat.dto;

import com.jeannimi.messenger.application.chat.dto.LastMessageStatus;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ChatDto(
    UUID id,
    String name,
    String type,
    List<ChatMemberDto> members,
    Instant createdAt,
    Instant lastMessageAt,
    LastMessageDto lastMessage,
    long unreadCount,
    LastMessageStatus lastMessageStatus) {}