package com.jeannimi.messenger.websocket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record SendMessageCommand(
    @NotNull(message = "Chat id is required") @Positive(message = "Chat id must be positive")
        Long chatId,
    @NotBlank(message = "Content is required") String content) {}
