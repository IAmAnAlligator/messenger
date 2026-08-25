package com.jeannimi.messenger.chat.dto;

public record ChatMemberReadDto(
    Long userId,
    Long lastReadMessageId) {}
