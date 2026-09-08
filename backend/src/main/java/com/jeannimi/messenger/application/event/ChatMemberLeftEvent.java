package com.jeannimi.messenger.application.event;

public record ChatMemberLeftEvent(Long chatId, Long userId) implements ApplicationEvent {}
