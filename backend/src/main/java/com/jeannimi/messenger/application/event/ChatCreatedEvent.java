package com.jeannimi.messenger.application.event;

import com.jeannimi.messenger.domain.chat.ChatId;
import com.jeannimi.messenger.domain.chat.ChatType;
import com.jeannimi.messenger.domain.user.UserId;
import java.util.List;

public record ChatCreatedEvent(ChatId chatId, String name, ChatType type, List<UserId> memberIds)
    implements ApplicationEvent {}
