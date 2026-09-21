package com.jeannimi.messenger.adapter.out.persistence.mapper;

import com.jeannimi.messenger.adapter.out.persistence.entity.ChatJpaEntity;
import com.jeannimi.messenger.adapter.out.persistence.entity.FileAttachmentJpaEntity;
import com.jeannimi.messenger.adapter.out.persistence.entity.MessageJpaEntity;
import com.jeannimi.messenger.adapter.out.persistence.entity.UserJpaEntity;
import com.jeannimi.messenger.application.message.dto.MessageWithSender;
import com.jeannimi.messenger.domain.message.FileAttachment;
import com.jeannimi.messenger.domain.message.Message;
import com.jeannimi.messenger.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessagePersistenceMapper {

  private final FileAttachmentPersistenceMapper fileAttachmentPersistenceMapper;
  private final UserPersistenceMapper userPersistenceMapper;

  public Message toDomain(MessageJpaEntity entity) {

    if (entity == null) {
      return null;
    }

    FileAttachment attachment = fileAttachmentPersistenceMapper.toDomain(entity.getAttachment());

    return Message.reconstitute(
        entity.getId(),
        entity.getChat().getId(),
        entity.getSender().getId(),
        entity.getContent(),
        entity.getCreatedAt(),
        entity.getType(),
        attachment);
  }

  public MessageJpaEntity toEntity(Message message, ChatJpaEntity chat, UserJpaEntity sender) {

    if (message == null) {
      return null;
    }

    FileAttachmentJpaEntity attachment =
        fileAttachmentPersistenceMapper.toEntity(message.getAttachment());

    return new MessageJpaEntity(
        message.getId(),
        chat,
        sender,
        message.getContent(),
        message.getCreatedAt(),
        message.getType(),
        attachment);
  }

  public MessageWithSender toMessageWithSender(MessageJpaEntity entity) {

    if (entity == null) {
      return null;
    }

    Message message = toDomain(entity);

    User sender = userPersistenceMapper.toDomain(entity.getSender());

    return new MessageWithSender(message, sender);
  }
}
