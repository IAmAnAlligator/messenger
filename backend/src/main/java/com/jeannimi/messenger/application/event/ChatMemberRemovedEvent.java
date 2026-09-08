package com.jeannimi.messenger.application.event;

public record ChatMemberRemovedEvent(Long chatId, Long userId) implements ApplicationEvent {}
