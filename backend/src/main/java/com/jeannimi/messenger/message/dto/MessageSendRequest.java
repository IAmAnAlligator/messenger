package com.jeannimi.messenger.message.dto;

import com.jeannimi.messenger.message.entity.Message;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MessageSendRequest(

    @NotBlank(message = "Message content is required")
    @Size(
        max = Message.MAX_CONTENT_LENGTH,
        message = "Message must not exceed {max} characters"
    )
    String content
) {}
