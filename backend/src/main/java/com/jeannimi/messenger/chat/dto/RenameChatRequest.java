package com.jeannimi.messenger.chat.dto;

import com.jeannimi.messenger.chat.entity.Chat;
import jakarta.validation.constraints.Size;

public record RenameChatRequest(

    @Size(min = Chat.MIN_CHAT_NAME_LENGTH, max = Chat.MAX_CHAT_NAME_LENGTH,
        message = "Chat name must be between {min} and {max} characters")
    String name

) {}
