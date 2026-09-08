package com.jeannimi.messenger.application.message.dto;

import com.jeannimi.messenger.application.user.dto.UserResult;
import java.time.Instant;

public record MessageResult(
    Long id,
    Long chatId,
    UserResult sender,
    String content,
    Instant createdAt,
    FileAttachmentResult attachment) {}
