package com.jeannimi.messenger.application.port.out;

import com.jeannimi.messenger.application.message.dto.MessageWithSender;
import com.jeannimi.messenger.domain.chat.ChatId;
import com.jeannimi.messenger.domain.message.FileAttachment;
import com.jeannimi.messenger.domain.message.Message;
import com.jeannimi.messenger.domain.message.MessageId;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MessageRepositoryPort {

  List<FileAttachment> findAttachmentsByChatId(ChatId chatId);

  Optional<MessageWithSender> findByIdAndChatId(MessageId messageId, ChatId chatId);

  List<MessageWithSender> findWithSenderByChatId(ChatId chatId, int limit);

  List<MessageWithSender> findWithSenderByChatIdAndCursor(
      ChatId chatId, Instant createdAt, MessageId id, int limit);

  int deleteByChatId(ChatId chatId);

  Message save(Message message);

  void delete(Message message);
}
