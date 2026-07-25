package com.jeannimi.messenger.kafka.event;

import java.util.UUID;

public record ChatMemberRemovedEvent(UUID eventId, Long chatId, Long userId) {}
