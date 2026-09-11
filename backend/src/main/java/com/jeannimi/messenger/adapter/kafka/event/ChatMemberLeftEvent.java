package com.jeannimi.messenger.adapter.kafka.event;

public record ChatMemberLeftEvent(Long chatId, Long userId) {}
