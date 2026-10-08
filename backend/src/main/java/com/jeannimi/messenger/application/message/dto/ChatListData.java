package com.jeannimi.messenger.application.message.dto;

import com.jeannimi.messenger.application.chat.dto.LastMessageStatus;
import com.jeannimi.messenger.domain.chat.ChatId;

public record ChatListData(
    ChatId chatId,
    LastMessageResult lastMessage,
    long unreadCount,
    LastMessageStatus lastMessageStatus) {}