package com.jeannimi.messenger.adapter.out.persistence;

import com.jeannimi.messenger.adapter.out.persistence.entity.ChatJpaEntity;
import com.jeannimi.messenger.adapter.out.persistence.entity.UserJpaEntity;
import com.jeannimi.messenger.adapter.out.persistence.mapper.ChatPersistenceMapper;
import com.jeannimi.messenger.application.port.out.ChatRepositoryPort;
import com.jeannimi.messenger.domain.chat.Chat;
import com.jeannimi.messenger.domain.chat.ChatId;
import com.jeannimi.messenger.domain.chat.ChatMember;
import com.jeannimi.messenger.domain.user.UserId;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
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
    Objects.requireNonNull(chat, "chat");
    Map<UUID, UserJpaEntity> users = loadUsers(chat);
    ChatJpaEntity entity =
        chatJpaRepository.findChatWithMembersById(chat.getId().value()).orElse(null);
    if (entity == null) {
      ChatJpaEntity newEntity = chatPersistenceMapper.toEntity(chat, users);
      ChatJpaEntity saved = chatJpaRepository.saveAndFlush(newEntity);
      return chatPersistenceMapper.toDomain(saved);
    }
    chatPersistenceMapper.updateEntity(chat, entity, users);
    chatJpaRepository.flush();
    return chatPersistenceMapper.toDomain(entity);
  }

  @Override
  public Optional<Chat> findById(ChatId chatId) {
    Objects.requireNonNull(chatId, "chatId");
    return chatJpaRepository.findById(chatId.value()).map(chatPersistenceMapper::toDomain);
  }

  @Override
  public Optional<Chat> findByIdWithMembers(ChatId chatId) {
    Objects.requireNonNull(chatId, "chatId");
    return chatJpaRepository
        .findChatWithMembersById(chatId.value())
        .map(chatPersistenceMapper::toDomain);
  }

  @Override
  public void delete(Chat chat) {
    Objects.requireNonNull(chat, "chat");
    chatJpaRepository.deleteById(chat.getId().value());
  }

  @Override
  public Optional<Chat> findByPrivateKey(String privateKey) {
    return chatJpaRepository.findByPrivateKey(privateKey).map(chatPersistenceMapper::toDomain);
  }

  @Override
  public List<Chat> findByIdsWithMembers(List<ChatId> ids) {
    if (ids == null || ids.isEmpty()) {
      return List.of();
    }
    List<UUID> uuidIds = ids.stream().map(ChatId::value).toList();
    return chatJpaRepository.findAllByIdIn(uuidIds).stream()
        .map(chatPersistenceMapper::toDomain)
        .toList();
  }

  private Map<UUID, UserJpaEntity> loadUsers(Chat chat) {
    List<UUID> userIds =
        chat.getMembers().stream()
            .map(ChatMember::getUserId)
            .map(UserId::value)
            .distinct()
            .toList();
    if (userIds.isEmpty()) {
      return Map.of();
    }
    Map<UUID, UserJpaEntity> users =
        userJpaRepository.findAllById(userIds).stream()
            .collect(Collectors.toMap(UserJpaEntity::getId, Function.identity()));
    if (users.size() != userIds.size()) {
      throw new IllegalStateException("Some chat members reference missing users");
    }
    return users;
  }
}
