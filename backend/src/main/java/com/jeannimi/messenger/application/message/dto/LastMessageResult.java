package com.jeannimi.messenger.application.message.dto;

import com.jeannimi.messenger.application.user.dto.UserResult;
import com.jeannimi.messenger.domain.message.MessageId;
import java.time.Instant;

public record LastMessageResult(
    MessageId id,
    UserResult sender,
    String content,
    Instant createdAt) {}