package com.jeannimi.messenger.chat.dto;

import com.jeannimi.messenger.chat.entity.Chat;
import jakarta.validation.constraints.Size;

public record RenameChatRequest(

    @Size(min = 1, max = Chat.MAX_CHAT_NAME_LENGTH)
    String name

) {}
