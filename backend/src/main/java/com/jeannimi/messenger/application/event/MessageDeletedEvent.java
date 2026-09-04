package com.jeannimi.messenger.application.event;

import java.time.Instant;

public record MessageDeletedEvent(
    Long messageId,
    Long chatId,
    Long deletedBy,
    Instant deletedAt) implements ApplicationEvent {
}