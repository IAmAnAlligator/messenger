package com.jeannimi.messenger.adapter.out.persistence.mapper;

import com.jeannimi.messenger.adapter.out.persistence.entity.ChatMemberJpaEntity;
import com.jeannimi.messenger.adapter.out.persistence.entity.UserJpaEntity;
import com.jeannimi.messenger.domain.chat.ChatMember;
import java.util.Objects;
import org.springframework.stereotype.Component;

@Component
public class ChatMemberPersistenceMapper {

  public ChatMember toDomain(ChatMemberJpaEntity entity) {

    if (entity == null) {
      return null;
    }

    return ChatMember.reconstitute(
        entity.getId(),
        entity.getUser().getId(),
        entity.getRole(),
        entity.getJoinedAt(),
        entity.getLastReadMessageId());
  }

  public ChatMemberJpaEntity toEntity(ChatMember member, UserJpaEntity user) {

    if (member == null) {
      return null;
    }

    Objects.requireNonNull(user, "user");

    return new ChatMemberJpaEntity(
        member.getId(),
        user,
        member.getRole(),
        member.getJoinedAt(),
        member.getLastReadMessageId());
  }

  public void updateEntity(ChatMember member, ChatMemberJpaEntity entity) {

    Objects.requireNonNull(member, "member");
    Objects.requireNonNull(entity, "entity");

    entity.updateLastReadMessageId(member.getLastReadMessageId());
  }
}
