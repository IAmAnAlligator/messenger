package com.jeannimi.messenger.kafka.event;

public record ChatMemberLeftEvent(Long chatId, Long userId) {}
