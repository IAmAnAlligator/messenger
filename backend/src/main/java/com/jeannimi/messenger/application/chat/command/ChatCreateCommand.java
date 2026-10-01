package com.jeannimi.messenger.application.chat.command;

import com.jeannimi.messenger.domain.chat.ChatType;
import java.util.List;
import java.util.UUID;

public record ChatCreateCommand(String name, List<UUID> memberIds, ChatType type) {}
