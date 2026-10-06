package com.jeannimi.messenger.adapter.out.persistence.mapper;

import com.jeannimi.messenger.adapter.out.persistence.entity.ChatJpaEntity;
import com.jeannimi.messenger.adapter.out.persistence.entity.FileAttachmentJpaEntity;
import com.jeannimi.messenger.adapter.out.persistence.entity.MessageJpaEntity;
import com.jeannimi.messenger.adapter.out.persistence.entity.UserJpaEntity;
import com.jeannimi.messenger.application.message.dto.MessageWithSender;
import com.jeannimi.messenger.application.port.out.MessageEncryptionPort;
import com.jeannimi.messenger.domain.chat.ChatId;
import com.jeannimi.messenger.domain.message.FileAttachment;
import com.jeannimi.messenger.domain.message.Message;
import com.jeannimi.messenger.domain.message.MessageId;
import com.jeannimi.messenger.domain.user.User;
import com.jeannimi.messenger.domain.user.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MessagePersistenceMapper {
  private final FileAttachmentPersistenceMapper fileAttachmentPersistenceMapper;
  private final UserPersistenceMapper userPersistenceMapper;
  private final MessageEncryptionPort messageEncryptionPort;

  public Message toDomain(MessageJpaEntity entity) {
    if (entity == null) {
      return null;
    }
    FileAttachment attachment = fileAttachmentPersistenceMapper.toDomain(entity.getAttachment());

    String content =
        entity.getEncryptedContent() == null
            ? null
            : messageEncryptionPort.decrypt(entity.getEncryptedContent());

    return Message.reconstitute(
        new MessageId(entity.getId()),
        new ChatId(entity.getChat().getId()),
        new UserId(entity.getSender().getId()),
        content,
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

    String encryptedContent =
        message.getContent() == null
            ? null
            : messageEncryptionPort.encrypt(message.getContent());

    return new MessageJpaEntity(
        message.getId().value(),
        chat,
        sender,
        encryptedContent,
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
