package com.jeannimi.messenger.adapter.out.persistence.mapper;

import com.jeannimi.messenger.adapter.out.persistence.entity.ChatJpaEntity;
import com.jeannimi.messenger.adapter.out.persistence.entity.ChatMemberJpaEntity;
import com.jeannimi.messenger.adapter.out.persistence.entity.UserJpaEntity;
import com.jeannimi.messenger.domain.chat.Chat;
import com.jeannimi.messenger.domain.chat.ChatId;
import com.jeannimi.messenger.domain.chat.ChatMember;
import com.jeannimi.messenger.domain.user.UserId;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ChatPersistenceMapper {
  private final ChatMemberPersistenceMapper chatMemberPersistenceMapper;

  public Chat toDomain(ChatJpaEntity entity) {
    if (entity == null) {
      return null;
    }
    return Chat.reconstitute(
        new ChatId(entity.getId()),
        entity.getName(),
        entity.getType(),
        entity.getMembers().stream()
            .map(chatMemberPersistenceMapper::toDomain)
            .collect(Collectors.toSet()),
        entity.getCreatedAt(),
        entity.getLastMessageAt(),
        entity.getPrivateKey());
  }

  /**
   * * Creates a new JPA entity from domain. * *
   *
   * <p>The domain Chat already contains its ChatId.
   */
  public ChatJpaEntity toEntity(Chat chat, Map<UUID, UserJpaEntity> users) {
    if (chat == null) {
      return null;
    }
    Objects.requireNonNull(users, "users");
    ChatJpaEntity entity =
        new ChatJpaEntity(
            chat.getId().value(),
            chat.getName(),
            chat.getType(),
            chat.getCreatedAt(),
            chat.getLastMessageAt(),
            chat.getPrivateKey());
    for (ChatMember member : chat.getMembers()) {
      UserJpaEntity user = getUser(member, users);
      ChatMemberJpaEntity memberEntity = chatMemberPersistenceMapper.toEntity(member, user);
      entity.addMember(memberEntity);
    }
    return entity;
  }

  /**
   * * Updates an existing JPA entity from the current domain state. * *
   *
   * <p>The existing entity must be managed by Hibernate.
   */
  public void updateEntity(Chat chat, ChatJpaEntity entity, Map<UUID, UserJpaEntity> users) {
    Objects.requireNonNull(chat, "chat");
    Objects.requireNonNull(entity, "entity");
    Objects.requireNonNull(users, "users");
    entity.update(chat.getName(), chat.getLastMessageAt());
    updateMembers(chat, entity, users);
  }

  private void updateMembers(Chat chat, ChatJpaEntity entity, Map<UUID, UserJpaEntity> users) {
    Set<UUID> domainUserIds =
        chat.getMembers().stream()
            .map(ChatMember::getUserId)
            .map(UserId::value)
            .collect(Collectors.toSet());
    Set<ChatMemberJpaEntity> membersToRemove =
        entity.getMembers().stream()
            .filter(existingMember -> !domainUserIds.contains(existingMember.getUser().getId()))
            .collect(Collectors.toSet());
    for (ChatMemberJpaEntity member : membersToRemove) {
      entity.removeMember(member);
    }
    Map<UUID, ChatMemberJpaEntity> existingMembers =
        entity.getMembers().stream()
            .collect(Collectors.toMap(member -> member.getUser().getId(), member -> member));
    for (ChatMember domainMember : chat.getMembers()) {
      UUID userId = domainMember.getUserId().value();
      ChatMemberJpaEntity existingMember = existingMembers.get(userId);
      if (existingMember == null) {
        UserJpaEntity user = getUser(domainMember, users);
        entity.addMember(chatMemberPersistenceMapper.toEntity(domainMember, user));
      } else {
        chatMemberPersistenceMapper.updateEntity(domainMember, existingMember);
      }
    }
  }

  private UserJpaEntity getUser(ChatMember member, Map<UUID, UserJpaEntity> users) {
    Objects.requireNonNull(member, "member");
    Objects.requireNonNull(users, "users");
    UUID userId = member.getUserId().value();
    UserJpaEntity user = users.get(userId);
    if (user == null) {
      throw new IllegalStateException("User not found for chat member: " + userId);
    }
    return user;
  }
}
