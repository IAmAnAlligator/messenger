package com.jeannimi.messenger.application.event;

import com.jeannimi.messenger.domain.chat.ChatId;
import com.jeannimi.messenger.domain.message.MessageId;
import com.jeannimi.messenger.domain.user.UserId;
import java.time.Instant;

public record MessageReadEvent(
    MessageId messageId,
    ChatId chatId,
    UserId readerId,
    UserId senderId,
    Instant readAt,
    MessageId lastReadMessageId
) implements ApplicationEvent {}
