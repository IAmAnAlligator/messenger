package com.jeannimi.messenger.adapter.out.persistence.mapper;

import com.jeannimi.messenger.adapter.out.persistence.projection.LastMessageProjection;
import com.jeannimi.messenger.application.message.dto.LastMessageResult;
import com.jeannimi.messenger.application.port.out.MessageEncryptionPort;
import com.jeannimi.messenger.application.user.dto.UserResult;
import com.jeannimi.messenger.domain.message.MessageId;
import com.jeannimi.messenger.domain.user.Role;
import com.jeannimi.messenger.domain.user.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class LastMessagePersistenceMapper {

  private final MessageEncryptionPort messageEncryptionPort;

  public LastMessageResult toResult(
      LastMessageProjection projection) {

    UserResult sender =
        new UserResult(
            new UserId(projection.getSenderId()),
            projection.getSenderHandle(),
            projection.getSenderUsername(),
            Role.valueOf(projection.getSenderRole()));

    String content =
        projection.getEncryptedContent() == null
            ? null
            : messageEncryptionPort.decrypt(
                projection.getEncryptedContent());

    return new LastMessageResult(
        new MessageId(projection.getMessageId()),
        sender,
        content,
        projection.getCreatedAt());
  }
}