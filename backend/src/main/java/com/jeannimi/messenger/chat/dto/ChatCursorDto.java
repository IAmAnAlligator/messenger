package com.jeannimi.messenger.chat.dto;

import java.time.Instant;

public record ChatCursorDto(Instant lastMessageAt, Long id) {}
