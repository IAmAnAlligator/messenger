package com.jeannimi.messenger.application.chat.dto;

import com.jeannimi.messenger.domain.message.MessageId;
import com.jeannimi.messenger.domain.user.UserId;

public record ChatMemberReadResult(UserId userId, MessageId lastReadMessageId) {}
