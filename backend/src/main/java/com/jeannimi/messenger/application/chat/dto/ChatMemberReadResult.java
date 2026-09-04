package com.jeannimi.messenger.application.chat.dto;

public record ChatMemberReadResult(
    Long userId,
    Long lastReadMessageId) {
}