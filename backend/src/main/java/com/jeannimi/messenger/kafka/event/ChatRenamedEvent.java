package com.jeannimi.messenger.kafka.event;

import java.util.UUID;

public record ChatRenamedEvent(UUID eventId, Long chatId, String oldName, String newName) {}
