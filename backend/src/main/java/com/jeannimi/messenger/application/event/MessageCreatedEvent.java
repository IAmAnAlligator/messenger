package com.jeannimi.messenger.application.event;

import com.jeannimi.messenger.application.message.dto.MessageResult;
import java.util.List;

public record MessageCreatedEvent(
    MessageResult message,
    List<Long> recipientUserIds
) implements ApplicationEvent {}
