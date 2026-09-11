package com.jeannimi.messenger.adapter.kafka.event;

public record ChatMemberRemovedEvent(Long chatId, Long userId) {}
