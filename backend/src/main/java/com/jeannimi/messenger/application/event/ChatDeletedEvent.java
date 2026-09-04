package com.jeannimi.messenger.application.event;

public record ChatDeletedEvent(
    Long chatId) implements ApplicationEvent {
}