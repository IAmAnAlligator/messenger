package com.jeannimi.messenger.adapter.in.websocket.dto;

public record PresenceStatus(
    String userId,
    boolean online) {
}