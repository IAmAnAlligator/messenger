package com.jeannimi.messenger.application.chat.command;

import com.jeannimi.messenger.chat.entity.ChatType;
import java.util.List;

public record ChatCreateCommand(
    String name,
    List<Long> memberIds,
    ChatType type
) {
}