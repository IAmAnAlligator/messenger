package com.jeannimi.messenger.application.event;

import com.jeannimi.messenger.domain.chat.ChatId;
import com.jeannimi.messenger.domain.user.UserId;
import java.util.List;

public record ChatRenamedEvent(
    ChatId chatId, String oldName, String newName, List<UserId> recipientUserIds)
    implements ApplicationEvent {}
