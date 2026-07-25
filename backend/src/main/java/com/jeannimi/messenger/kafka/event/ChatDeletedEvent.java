package com.jeannimi.messenger.kafka.event;

import java.util.UUID;

public record ChatDeletedEvent(UUID eventId, Long chatId) {}
