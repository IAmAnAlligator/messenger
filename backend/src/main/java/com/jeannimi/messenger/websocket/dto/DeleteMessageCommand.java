package com.jeannimi.messenger.websocket.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record DeleteMessageCommand(
    @NotNull(message = "Chat id is required") @Positive(message = "Chat id must be positive")
        Long chatId,
    @NotNull(message = "Message id is required")
        @Positive(message = "Message id must be positive")
        @JsonAlias("id")
        Long messageId) {}
