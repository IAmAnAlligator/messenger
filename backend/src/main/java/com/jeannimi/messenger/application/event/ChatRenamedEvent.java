package com.jeannimi.messenger.application.event;

public record ChatRenamedEvent(
    Long chatId,
    String oldName,
    String newName) implements ApplicationEvent {
}