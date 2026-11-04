package com.jeannimi.messenger.application.notification.dto;

import com.jeannimi.messenger.domain.chat.ChatId;
import com.jeannimi.messenger.domain.chat.ChatType;
import com.jeannimi.messenger.domain.event.EventId;
import com.jeannimi.messenger.domain.message.MessageId;
import com.jeannimi.messenger.domain.user.UserId;
import java.util.List;

public record MessageNotification(
    EventId eventId,
    MessageId messageId,
    ChatId chatId,
    ChatType chatType,
    String chatName,
    UserId senderId,
    String senderUsername,
    List<UserId> recipientUserIds) {}
