package com.jeannimi.messenger.message.dto;

import com.jeannimi.messenger.message.MessageConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MessageSendRequest(
    @NotBlank(message = "Message content is required")
        @Size(
            max = MessageConstants.MAX_CONTENT_LENGTH,
            message = "Message must not exceed {max} characters")
        String content) {}
