package com.jeannimi.messenger.dto;

public record ChatCreatedEvent(String type, Long chatId, String name) {}
