package com.jeannimi.messenger.application.port.out;

import com.jeannimi.messenger.message.entity.FileAttachment;
import com.jeannimi.messenger.message.entity.Message;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface MessageRepositoryPort {

  List<FileAttachment> findAttachmentsByChatId(Long chatId);

  List<Message> findAllByChatId(
      Long chatId,
      int limit);

  List<Message> findAllByChatId(Long chatId);

  Optional<Message> findByIdAndChatId(
      Long messageId,
      Long chatId);

  List<Message> findWithSenderByChatId(
      Long chatId,
      int limit);

  List<Message> findWithSenderByChatIdAndCursor(
      Long chatId,
      Instant createdAt,
      Long id,
      int limit);

  int deleteByChatId(Long chatId);

  Message save(Message message);

  void delete(Message message);
}
