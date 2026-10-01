package com.jeannimi.messenger.application.event;

import com.jeannimi.messenger.domain.chat.ChatId;
import com.jeannimi.messenger.domain.user.UserId;

public record ChatMemberLeftEvent(ChatId chatId, UserId userId) implements ApplicationEvent {}
