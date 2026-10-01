package com.jeannimi.messenger.application.event;

import com.jeannimi.messenger.domain.chat.ChatId;
import com.jeannimi.messenger.domain.user.UserId;

public record ChatMemberAddedEvent(ChatId chatId, UserId userId, String username)
    implements ApplicationEvent {}
