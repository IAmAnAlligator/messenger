package com.jeannimi.messenger.application.event;

public record ChatMemberAddedEvent(Long chatId, Long userId, String username)
    implements ApplicationEvent {}
