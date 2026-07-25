package com.jeannimi.messenger.kafka.event;

import java.util.UUID;

public record ChatMemberLeftEvent(UUID eventId, Long chatId, Long userId) {}
