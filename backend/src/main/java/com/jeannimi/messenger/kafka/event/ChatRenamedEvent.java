package com.jeannimi.messenger.kafka.event;

public record ChatRenamedEvent(Long chatId, String oldName, String newName) {}
