package com.jeannimi.messenger.application.message.dto;

import com.jeannimi.messenger.application.user.dto.UserResult;
import com.jeannimi.messenger.domain.chat.ChatId;
import com.jeannimi.messenger.domain.message.MessageId;
import java.time.Instant;
import java.util.UUID;

public record MessageResult(
    MessageId id,
    ChatId chatId,
    UserResult sender,
    String content,
    Instant createdAt,
    FileAttachmentResult attachment) {}
