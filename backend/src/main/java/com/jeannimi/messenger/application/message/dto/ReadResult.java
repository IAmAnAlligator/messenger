package com.jeannimi.messenger.application.message.dto;

import com.jeannimi.messenger.application.chat.dto.ChatMemberReadResult;

public record ReadResult(
    ChatMemberReadResult read,
    boolean changed) {
}