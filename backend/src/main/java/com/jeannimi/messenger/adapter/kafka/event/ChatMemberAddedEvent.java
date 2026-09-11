package com.jeannimi.messenger.adapter.kafka.event;

public record ChatMemberAddedEvent(Long chatId, Long userId, String username) {}
