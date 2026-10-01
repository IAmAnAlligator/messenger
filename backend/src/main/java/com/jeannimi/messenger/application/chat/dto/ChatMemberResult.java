package com.jeannimi.messenger.application.chat.dto;

import com.jeannimi.messenger.application.user.dto.UserResult;
import com.jeannimi.messenger.domain.chat.ChatRole;
import com.jeannimi.messenger.domain.message.MessageId;
import java.time.Instant;

public record ChatMemberResult(
    UserResult user, ChatRole chatRole, Instant joinedAt, MessageId lastReadMessageId) {}
