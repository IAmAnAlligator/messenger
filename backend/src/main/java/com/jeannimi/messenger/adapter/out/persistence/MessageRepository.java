package com.jeannimi.messenger.adapter.out.persistence;

import com.jeannimi.messenger.adapter.out.persistence.entity.ChatJpaEntity;
import com.jeannimi.messenger.adapter.out.persistence.entity.MessageJpaEntity;
import com.jeannimi.messenger.adapter.out.persistence.entity.UserJpaEntity;
import com.jeannimi.messenger.adapter.out.persistence.mapper.FileAttachmentPersistenceMapper;
import com.jeannimi.messenger.adapter.out.persistence.mapper.MessagePersistenceMapper;
import com.jeannimi.messenger.application.port.out.MessageRepositoryPort;
import com.jeannimi.messenger.domain.message.FileAttachment;
import com.jeannimi.messenger.domain.message.Message;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class MessageRepository implements MessageRepositoryPort {

  private final MessageJpaRepository messageJpaRepository;
  private final ChatJpaRepository chatJpaRepository;
  private final UserJpaRepository userJpaRepository;
  private final MessagePersistenceMapper messagePersistenceMapper;
  private final FileAttachmentPersistenceMapper fileAttachmentPersistenceMapper;

  @Override
  @Transactional(readOnly = true)
  public List<FileAttachment> findAttachmentsByChatId(Long chatId) {

    return messageJpaRepository.findAttachmentsByChatId(chatId).stream()
        .map(fileAttachmentPersistenceMapper::toDomain)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<Message> findAllByChatId(Long chatId, int limit) {

    return messageJpaRepository.findAllByChat_Id(chatId, PageRequest.of(0, limit)).stream()
        .map(messagePersistenceMapper::toDomain)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<Message> findAllByChatId(Long chatId) {

    return messageJpaRepository.findAllByChat_Id(chatId).stream()
        .map(messagePersistenceMapper::toDomain)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public Optional<Message> findByIdAndChatId(Long messageId, Long chatId) {

    return messageJpaRepository
        .findByIdAndChatId(messageId, chatId)
        .map(messagePersistenceMapper::toDomain);
  }

  @Override
  @Transactional(readOnly = true)
  public List<Message> findWithSenderByChatId(Long chatId, int limit) {

    return messageJpaRepository.findWithSenderByChatId(chatId, PageRequest.of(0, limit)).stream()
        .map(messagePersistenceMapper::toDomain)
        .toList();
  }

  @Override
  @Transactional(readOnly = true)
  public List<Message> findWithSenderByChatIdAndCursor(
      Long chatId, Instant createdAt, Long id, int limit) {

    return messageJpaRepository
        .findWithSenderByChatIdAndCursor(chatId, createdAt, id, PageRequest.of(0, limit))
        .stream()
        .map(messagePersistenceMapper::toDomain)
        .toList();
  }

  @Override
  @Transactional
  public int deleteByChatId(Long chatId) {

    return messageJpaRepository.deleteByChatId(chatId);
  }

  @Override
  @Transactional
  public Message save(Message message) {

    ChatJpaEntity chat = chatJpaRepository.getReferenceById(message.getChatId());

    UserJpaEntity sender = userJpaRepository.getReferenceById(message.getSenderId());

    MessageJpaEntity entity = messagePersistenceMapper.toEntity(message, chat, sender);

    MessageJpaEntity saved = messageJpaRepository.save(entity);

    return messagePersistenceMapper.toDomain(saved);
  }

  @Override
  @Transactional
  public void delete(Message message) {

    if (message == null || message.getId() == null) {
      return;
    }

    messageJpaRepository.deleteById(message.getId());
  }
}
