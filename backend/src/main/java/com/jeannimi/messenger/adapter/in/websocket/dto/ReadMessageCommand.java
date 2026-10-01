package com.jeannimi.messenger.adapter.in.websocket.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.UUID;

public record ReadMessageCommand(
    @JsonAlias("id")
        @NotNull(message = "Message id is required")
    UUID messageId,
    @NotNull(message = "Chat id is required")
        UUID chatId) {}
