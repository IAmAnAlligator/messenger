package com.jeannimi.messenger.kafka.event;

public record ChatMemberAddedEvent(Long chatId, Long userId, String username) {}
