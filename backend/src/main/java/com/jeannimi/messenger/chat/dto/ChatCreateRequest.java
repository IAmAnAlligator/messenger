package com.jeannimi.messenger.chat.dto;

import com.jeannimi.messenger.chat.ChatConstants;
import com.jeannimi.messenger.chat.entity.ChatType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ChatCreateRequest(
    @NotNull(message = "Chat type is required") ChatType type,

    // @NotBlank(message = "Chat name is required")
    @Size(
            min = ChatConstants.MIN_CHAT_NAME_LENGTH,
            max = ChatConstants.MAX_CHAT_NAME_LENGTH,
            message = "Chat name must be between {min} and {max} characters")
        String name,
    @NotEmpty(message = "At least one member is required")
        @Size(
            max = ChatConstants.MAX_GROUP_MEMBERS - 1,
            message = "Chat cannot have more than {max} members")
        List<@NotNull(message = "Member id cannot be null") Long> memberIds) {}
