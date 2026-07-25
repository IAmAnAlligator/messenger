package com.jeannimi.messenger.kafka.event;

import com.jeannimi.messenger.chat.entity.ChatType;
import java.util.List;
import java.util.UUID;

public record ChatCreatedEvent(
    UUID eventId, Long chatId, String name, ChatType type, List<Long> memberIds) {}
