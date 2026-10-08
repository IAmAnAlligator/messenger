package com.jeannimi.messenger.application.chat.dto;

import com.jeannimi.messenger.application.message.dto.LastMessageResult;
import com.jeannimi.messenger.domain.chat.ChatId;
import java.time.Instant;
import java.util.List;

public record ChatResult(
    ChatId id,
    String name,
    String type,
    List<ChatMemberResult> members,
    Instant createdAt,
    Instant lastMessageAt,
    LastMessageResult lastMessage,
    long unreadCount,
    LastMessageStatus lastMessageStatus) {}