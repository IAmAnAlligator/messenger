package com.jeannimi.messenger.application.event;

import java.util.List;

public record ChatRenamedEvent(
    Long chatId, String oldName, String newName, List<Long> recipientUserIds)
    implements ApplicationEvent {}
