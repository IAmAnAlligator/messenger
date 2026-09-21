package com.jeannimi.messenger.application.port.out;

import com.jeannimi.messenger.application.message.dto.MessageWithSender;
import com.jeannimi.messenger.domain.message.FileAttachment;
import com.jeannimi.messenger.domain.message.Message;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MessageRepositoryPort {

  List<FileAttachment> findAttachmentsByChatId(Long chatId);

  Optional<MessageWithSender> findByIdAndChatId(Long messageId, Long chatId);

  List<MessageWithSender> findWithSenderByChatId(Long chatId, int limit);

  List<MessageWithSender> findWithSenderByChatIdAndCursor(
      Long chatId, Instant createdAt, Long id, int limit);

  int deleteByChatId(Long chatId);

  Message save(Message message);

  void delete(Message message);
}
