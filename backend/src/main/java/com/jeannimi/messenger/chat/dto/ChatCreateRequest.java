package com.jeannimi.messenger.chat.dto;

import com.jeannimi.messenger.chat.entity.Chat;
import com.jeannimi.messenger.chat.entity.ChatType;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;

public record ChatCreateRequest(

    @NotNull ChatType type,

    @Size(min = 1, max = Chat.MAX_CHAT_NAME_LENGTH)
    String name,

    @NotEmpty
    @Size(max = Chat.MAX_GROUP_MEMBERS - 1)
    List<@NotNull Long> memberIds) {}
