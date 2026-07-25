package com.jeannimi.messenger.kafka.event;

import java.time.Instant;

public record MessageDeletedEvent(
    Long messageId,
    Long chatId,
    Long deletedBy,
    Instant deletedAt
){}
