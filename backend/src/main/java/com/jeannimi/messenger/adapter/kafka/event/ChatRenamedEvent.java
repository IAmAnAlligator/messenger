package com.jeannimi.messenger.adapter.kafka.event;

public record ChatRenamedEvent(Long chatId, String oldName, String newName) {}
