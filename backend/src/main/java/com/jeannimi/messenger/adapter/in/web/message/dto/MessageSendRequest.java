package com.jeannimi.messenger.adapter.in.web.message.dto;

import com.jeannimi.messenger.domain.message.MessageConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MessageSendRequest(
    @NotBlank(message = "Message content is required")
        @Size(
            max = MessageConstants.MAX_CONTENT_LENGTH,
            message = "Message must not exceed {max} characters")
        String content) {}
