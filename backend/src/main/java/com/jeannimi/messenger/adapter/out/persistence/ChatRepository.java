package com.jeannimi.messenger.adapter.out.persistence;

import com.jeannimi.messenger.application.port.out.ChatRepositoryPort;
import com.jeannimi.messenger.chat.entity.Chat;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChatRepository implements ChatRepositoryPort {

  private final ChatJpaRepository chatJpaRepository;

  @Override
  public Chat save(Chat chat) {
    return chatJpaRepository.save(chat);
  }

  @Override
  public Optional<Chat> findById(Long chatId) {
    return chatJpaRepository.findById(chatId);
  }

  @Override
  public void delete(Chat chat) {
    chatJpaRepository.delete(chat);
  }

  @Override
  public boolean existsById(Long chatId) {
    return chatJpaRepository.existsById(chatId);
  }

  @Override
  public Optional<Chat> findByIdWithMembers(Long chatId) {
    return chatJpaRepository.findByIdWithMembers(chatId);
  }

  @Override
  public Optional<Chat> findByPrivateKey(String privateKey) {
    return chatJpaRepository.findByPrivateKey(privateKey);
  }

  @Override
  public List<Chat> findByIdsWithMembers(List<Long> ids) {
    return chatJpaRepository.findByIdsWithMembers(ids);
  }
}
