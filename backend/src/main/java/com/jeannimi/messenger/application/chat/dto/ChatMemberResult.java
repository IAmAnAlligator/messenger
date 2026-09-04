package com.jeannimi.messenger.application.chat.dto;

import com.jeannimi.messenger.application.user.dto.UserResult;
import com.jeannimi.messenger.chat.entity.ChatRole;
import java.time.Instant;

public record ChatMemberResult(
    UserResult user,
    ChatRole chatRole,
    Instant joinedAt,
    Long lastReadMessageId) {

}
