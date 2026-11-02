package com.jeannimi.messenger.adapter.kafka.event;

import com.jeannimi.messenger.application.event.MessageCreatedEvent;
import com.jeannimi.messenger.application.message.dto.FileAttachmentResult;
import com.jeannimi.messenger.application.message.dto.MessageResult;
import com.jeannimi.messenger.application.user.dto.UserResult;
import com.jeannimi.messenger.domain.chat.ChatId;
import com.jeannimi.messenger.domain.chat.ChatType;
import com.jeannimi.messenger.domain.message.MessageId;
import com.jeannimi.messenger.domain.user.UserId;
import java.time.Instant;
import java.util.List;

public record MessageSentEvent(
    MessageId messageId,
    ChatId chatId,
    ChatType chatType,
    String chatName,
    UserResult sender,
    String content,
    Instant createdAt,
    FileAttachmentResult attachment,
    List<UserId> recipientUserIds
) {

  public static MessageSentEvent from(MessageCreatedEvent event) {
    MessageResult result = event.message();

    return new MessageSentEvent(
        result.id(),
        result.chatId(),
        event.chatType(),
        event.chatName(),
        result.sender(),
        result.content(),
        result.createdAt(),
        result.attachment(),
        event.recipientUserIds()
    );
  }

  public MessageResult toResult() {
    return new MessageResult(
        messageId,
        chatId,
        sender,
        content,
        createdAt,
        attachment
    );
  }
}