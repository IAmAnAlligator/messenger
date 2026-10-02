package com.jeannimi.messenger.adapter.in.websocket.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record SendMessageCommand(
    @NotNull(message = "Chat id is required") UUID chatId,
    @NotBlank(message = "Content is required") String content) {}
