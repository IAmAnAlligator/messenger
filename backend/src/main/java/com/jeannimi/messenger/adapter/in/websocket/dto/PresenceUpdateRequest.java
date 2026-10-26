package com.jeannimi.messenger.adapter.in.websocket.dto;

public record PresenceUpdateRequest(
    String chatId,
    boolean visible
) {
}