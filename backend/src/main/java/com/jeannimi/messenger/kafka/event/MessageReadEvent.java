package com.jeannimi.messenger.kafka.event;

import java.time.Instant;

public record MessageReadEvent(
    Long messageId,
    Long chatId,
    Long readerId,
    Instant readAt
){}
