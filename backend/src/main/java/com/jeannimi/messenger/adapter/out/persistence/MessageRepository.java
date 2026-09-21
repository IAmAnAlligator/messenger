package com.jeannimi.messenger.adapter.out.persistence;

import com.jeannimi.messenger.adapter.out.persistence.entity.ChatJpaEntity;
import com.jeannimi.messenger.adapter.out.persistence.entity.MessageJpaEntity;
import com.jeannimi.messenger.adapter.out.persistence.entity.UserJpaEntity;
import com.jeannimi.messenger.adapter.out.persistence.mapper.FileAttachmentPersistenceMapper;
import com.jeannimi.messenger.adapter.out.persistence.mapper.MessagePersistenceMapper;
import com.jeannimi.messenger.application.message.dto.MessageWithSender;
import com.jeannimi.messenger.application.port.out.MessageRepositoryPort;
import com.jeannimi.messenger.domain.message.FileAttachment;
import com.jeannimi.messenger.domain.message.Message;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MessageRepository implements MessageRepositoryPort {

  private final MessageJpaRepository messageJpaRepository;
  private final ChatJpaRepository chatJpaRepository;
  private final UserJpaRepository userJpaRepository;
  private final MessagePersistenceMapper messagePersistenceMapper;
  private final FileAttachmentPersistenceMapper fileAttachmentPersistenceMapper;

  @Override
  public List<FileAttachment> findAttachmentsByChatId(Long chatId) {

    return messageJpaRepository.findAttachmentsByChatId(chatId).stream()
        .map(fileAttachmentPersistenceMapper::toDomain)
        .toList();
  }

  @Override
  public Optional<MessageWithSender> findByIdAndChatId(Long messageId, Long chatId) {

    return messageJpaRepository
        .findByIdAndChatId(messageId, chatId)
        .map(messagePersistenceMapper::toMessageWithSender);
  }

  @Override
  public List<MessageWithSender> findWithSenderByChatId(Long chatId, int limit) {

    return messageJpaRepository.findWithSenderByChatId(chatId, PageRequest.of(0, limit)).stream()
        .map(messagePersistenceMapper::toMessageWithSender)
        .toList();
  }

  @Override
  public List<MessageWithSender> findWithSenderByChatIdAndCursor(
      Long chatId, Instant createdAt, Long id, int limit) {

    return messageJpaRepository
        .findWithSenderByChatIdAndCursor(chatId, createdAt, id, PageRequest.of(0, limit))
        .stream()
        .map(messagePersistenceMapper::toMessageWithSender)
        .toList();
  }

  @Override
  public int deleteByChatId(Long chatId) {

    return messageJpaRepository.deleteByChatId(chatId);
  }

  @Override
  public Message save(Message message) {

    ChatJpaEntity chat = chatJpaRepository.getReferenceById(message.getChatId());

    UserJpaEntity sender = userJpaRepository.getReferenceById(message.getSenderId());

    MessageJpaEntity entity = messagePersistenceMapper.toEntity(message, chat, sender);

    MessageJpaEntity saved = messageJpaRepository.save(entity);

    return messagePersistenceMapper.toDomain(saved);
  }

  @Override
  public void delete(Message message) {

    if (message == null || message.getId() == null) {
      return;
    }

    messageJpaRepository.deleteById(message.getId());
  }
}
