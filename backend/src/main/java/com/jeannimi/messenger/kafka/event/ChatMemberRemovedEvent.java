package com.jeannimi.messenger.kafka.event;

public record ChatMemberRemovedEvent(Long chatId, Long userId) {}
