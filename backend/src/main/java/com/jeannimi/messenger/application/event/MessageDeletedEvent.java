package com.jeannimi.messenger.application.event;

import com.jeannimi.messenger.domain.chat.ChatId;
import com.jeannimi.messenger.domain.message.MessageId;
import com.jeannimi.messenger.domain.user.UserId;
import java.time.Instant;

public record MessageDeletedEvent(MessageId messageId, ChatId chatId, UserId deletedBy, Instant deletedAt)
    implements ApplicationEvent {}
