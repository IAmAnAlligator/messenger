package com.jeannimi.messenger.adapter.in.web.chat.dto;

import com.jeannimi.messenger.domain.chat.ChatConstants;
import com.jeannimi.messenger.domain.chat.ChatType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import java.util.UUID;

public record ChatCreateRequest(
    @NotNull(message = "Chat type is required") ChatType type,
    @Size(
            max = ChatConstants.MAX_CHAT_NAME_LENGTH,
            message = "Chat name must not exceed {max} characters")
        String name,
    @NotEmpty(message = "At least one member is required")
        @Size(
            max = ChatConstants.MAX_GROUP_MEMBERS - 1,
            message = "Chat cannot have more than {max} members")
        List<@NotNull(message = "Member id cannot be null") UUID> memberIds) {}
