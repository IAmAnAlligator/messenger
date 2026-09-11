package com.jeannimi.messenger.adapter.kafka.event;

import java.time.Instant;

public record MessageReadEvent(
    Long messageId, Long chatId, Long readerId, Instant readAt, Long lastReadMessageId) {}
