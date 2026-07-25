package com.jeannimi.messenger.kafka.event;

import java.util.UUID;

public record ChatMemberAddedEvent(UUID eventId, Long chatId, Long userId, String username) {}
