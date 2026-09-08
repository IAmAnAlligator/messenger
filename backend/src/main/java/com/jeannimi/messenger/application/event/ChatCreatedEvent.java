package com.jeannimi.messenger.application.event;

import com.jeannimi.messenger.domain.chat.ChatType;
import java.util.List;

public record ChatCreatedEvent(Long chatId, String name, ChatType type, List<Long> memberIds)
    implements ApplicationEvent {}
