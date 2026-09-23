package com.jeannimi.messenger.application.event;

import java.util.List;

public record ChatDeletedEvent(Long chatId, List<Long> recipientUserIds)
    implements ApplicationEvent {}
