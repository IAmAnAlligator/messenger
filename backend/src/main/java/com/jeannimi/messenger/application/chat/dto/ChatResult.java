package com.jeannimi.messenger.application.chat.dto;

import java.time.Instant;
import java.util.List;

public record ChatResult(
    Long id,
    String name,
    String type,
    List<ChatMemberResult> members,
    Instant createdAt,
    Instant lastMessageAt) {

}
