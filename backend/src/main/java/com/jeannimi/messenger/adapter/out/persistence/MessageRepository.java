package com.jeannimi.messenger.adapter.out.persistence;

import com.jeannimi.messenger.application.port.out.MessageRepositoryPort;
import com.jeannimi.messenger.message.entity.FileAttachment;
import com.jeannimi.messenger.message.entity.Message;
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

  @Override
  public List<FileAttachment> findAttachmentsByChatId(Long chatId) {
    return messageJpaRepository.findAttachmentsByChatId(chatId);
  }

  @Override
  public List<Message> findAllByChatId(
      Long chatId,
      int limit) {

    return messageJpaRepository.findAllByChatId(
        chatId,
        PageRequest.of(0, limit));
  }

  @Override
  public List<Message> findAllByChatId(Long chatId) {
    return messageJpaRepository.findAllByChatId(chatId);
  }

  @Override
  public Optional<Message> findByIdAndChatId(
      Long messageId,
      Long chatId) {

    return messageJpaRepository.findByIdAndChatId(
        messageId,
        chatId);
  }

  @Override
  public List<Message> findWithSenderByChatId(
      Long chatId,
      int limit) {

    return messageJpaRepository.findWithSenderByChatId(
        chatId,
        PageRequest.of(0, limit));
  }

  @Override
  public List<Message> findWithSenderByChatIdAndCursor(
      Long chatId,
      Instant createdAt,
      Long id,
      int limit) {

    return messageJpaRepository.findWithSenderByChatIdAndCursor(
        chatId,
        createdAt,
        id,
        PageRequest.of(0, limit));
  }

  @Override
  public int deleteByChatId(Long chatId) {
    return messageJpaRepository.deleteByChatId(chatId);
  }

  @Override
  public Message save(Message message) {
    return messageJpaRepository.save(message);
  }

  @Override
  public void delete(Message message) {
    messageJpaRepository.delete(message);
  }
}
