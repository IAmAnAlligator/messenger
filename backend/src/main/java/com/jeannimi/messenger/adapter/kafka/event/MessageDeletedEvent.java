package com.jeannimi.messenger.adapter.kafka.event;

import java.time.Instant;

public record MessageDeletedEvent(Long messageId, Long chatId, Long deletedBy, Instant deletedAt) {}
