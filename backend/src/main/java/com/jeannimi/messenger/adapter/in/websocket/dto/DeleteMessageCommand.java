package com.jeannimi.messenger.adapter.in.websocket.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record DeleteMessageCommand(
    @NotNull(message = "Chat id is required") UUID chatId,
    @NotNull(message = "Message id is required") @JsonAlias("id") UUID messageId) {}
