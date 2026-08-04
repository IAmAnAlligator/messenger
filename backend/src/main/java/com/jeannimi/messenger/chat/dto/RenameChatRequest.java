package com.jeannimi.messenger.chat.dto;

import com.jeannimi.messenger.chat.ChatConstants;
import jakarta.validation.constraints.Size;

public record RenameChatRequest(
    @Size(
            min = ChatConstants.MIN_CHAT_NAME_LENGTH,
            max = ChatConstants.MAX_CHAT_NAME_LENGTH,
            message = "Chat name must be between {min} and {max} characters")
        String name) {}
