package com.jeannimi.messenger.adapter.out.persistence;

import com.jeannimi.messenger.adapter.out.persistence.entity.ChatJpaEntity;
import com.jeannimi.messenger.adapter.out.persistence.entity.UserJpaEntity;
import com.jeannimi.messenger.adapter.out.persistence.mapper.ChatPersistenceMapper;
import com.jeannimi.messenger.application.port.out.ChatRepositoryPort;
import com.jeannimi.messenger.domain.chat.Chat;
import com.jeannimi.messenger.domain.chat.ChatMember;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ChatRepository implements ChatRepositoryPort {

  private final ChatJpaRepository chatJpaRepository;
  private final UserJpaRepository userJpaRepository;
  private final ChatPersistenceMapper chatPersistenceMapper;

  @Override
  public Chat save(Chat chat) {

    Map<Long, UserJpaEntity> users = loadUsers(chat);

    if (chat.getId() == null) {

      ChatJpaEntity entity = chatPersistenceMapper.toEntity(chat, users);

      ChatJpaEntity saved = chatJpaRepository.save(entity);

      return chatPersistenceMapper.toDomain(saved);
    }

    ChatJpaEntity entity =
        chatJpaRepository
            .findById(chat.getId())
            .orElseThrow(() -> new IllegalStateException("Chat not found: " + chat.getId()));

    chatPersistenceMapper.updateEntity(chat, entity, users);

    chatJpaRepository.flush();

    return chatPersistenceMapper.toDomain(entity);
  }

  @Override
  public Optional<Chat> findById(Long chatId) {

    return chatJpaRepository.findById(chatId).map(chatPersistenceMapper::toDomain);
  }

  @Override
  public void delete(Chat chat) {

    if (chat != null && chat.getId() != null) {
      chatJpaRepository.deleteById(chat.getId());
    }
  }

  @Override
  public boolean existsById(Long chatId) {

    return chatJpaRepository.existsById(chatId);
  }

  @Override
  public Optional<Chat> findByIdWithMembers(Long chatId) {

    return chatJpaRepository.findById(chatId).map(chatPersistenceMapper::toDomain);
  }

  @Override
  public Optional<Chat> findByPrivateKey(String privateKey) {

    return chatJpaRepository.findByPrivateKey(privateKey).map(chatPersistenceMapper::toDomain);
  }

  @Override
  public List<Chat> findByIdsWithMembers(List<Long> ids) {

    if (ids == null || ids.isEmpty()) {
      return List.of();
    }

    return chatJpaRepository.findAllByIdIn(ids).stream()
        .map(chatPersistenceMapper::toDomain)
        .toList();
  }

  private Map<Long, UserJpaEntity> loadUsers(Chat chat) {

    List<Long> userIds = chat.getMembers().stream().map(ChatMember::getUserId).distinct().toList();

    if (userIds.isEmpty()) {
      return Map.of();
    }

    return userJpaRepository.findAllById(userIds).stream()
        .collect(Collectors.toMap(UserJpaEntity::getId, Function.identity()));
  }
}
