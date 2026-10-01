package com.jeannimi.messenger.application.event;

import com.jeannimi.messenger.application.message.dto.MessageResult;
import com.jeannimi.messenger.domain.user.UserId;
import java.util.List;

public record MessageCreatedEvent(MessageResult message, List<UserId> recipientUserIds)
    implements ApplicationEvent {}
