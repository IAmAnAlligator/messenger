package com.jeannimi.messenger.adapter.out.persistence;


import com.jeannimi.messenger.adapter.out.persistence.entity.ChatJpaEntity;
import com.jeannimi.messenger.adapter.out.persistence.entity.MessageJpaEntity;
import com.jeannimi.messenger.adapter.out.persistence.entity.UserJpaEntity;
import com.jeannimi.messenger.adapter.out.persistence.mapper.FileAttachmentPersistenceMapper;
import com.jeannimi.messenger.adapter.out.persistence.mapper.LastMessagePersistenceMapper;
import com.jeannimi.messenger.adapter.out.persistence.mapper.MessagePersistenceMapper;
import com.jeannimi.messenger.adapter.out.persistence.projection.LastMessageProjection;
import com.jeannimi.messenger.adapter.out.persistence.projection.LastMessageStatusProjection;
import com.jeannimi.messenger.adapter.out.persistence.projection.UnreadCountProjection;
import com.jeannimi.messenger.application.chat.dto.LastMessageStatus;
import com.jeannimi.messenger.application.message.dto.ChatListData;
import com.jeannimi.messenger.application.message.dto.LastMessageResult;
import com.jeannimi.messenger.application.message.dto.MessageWithSender;
import com.jeannimi.messenger.application.port.out.MessageRepositoryPort;
import com.jeannimi.messenger.domain.chat.ChatId;
import com.jeannimi.messenger.domain.message.FileAttachment;
import com.jeannimi.messenger.domain.message.Message;
import com.jeannimi.messenger.domain.message.MessageId;
import com.jeannimi.messenger.domain.user.UserId;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MessageRepository implements MessageRepositoryPort {

  private final MessageJpaRepository messageJpaRepository;
  private final ChatJpaRepository chatJpaRepository;
  private final UserJpaRepository userJpaRepository;
  private final MessagePersistenceMapper messagePersistenceMapper;
  private final FileAttachmentPersistenceMapper fileAttachmentPersistenceMapper;
  private final LastMessagePersistenceMapper lastMessagePersistenceMapper;

  @Override
  public List<ChatListData> findChatListData(
      UserId userId,
      List<ChatId> chatIds) {

    if (chatIds.isEmpty()) {
      return List.of();
    }

    List<UUID> ids =
        chatIds.stream()
            .map(ChatId::value)
            .toList();

    List<LastMessageProjection> lastMessages =
        messageJpaRepository.findLastMessagesByChatIds(ids);

    List<UnreadCountProjection> unreadCounts =
        messageJpaRepository.findUnreadCounts(
            userId.value(),
            ids);

    List<LastMessageStatusProjection> statuses =
        messageJpaRepository.findLastMessageStatuses(
            userId.value(),
            ids);

    Map<UUID, LastMessageProjection> lastMessageByChatId =
        lastMessages.stream()
            .collect(Collectors.toMap(
                LastMessageProjection::getChatId,
                Function.identity()));

    Map<UUID, Long> unreadCountByChatId =
        unreadCounts.stream()
            .collect(Collectors.toMap(
                UnreadCountProjection::getChatId,
                UnreadCountProjection::getUnreadCount));

    Map<UUID, LastMessageStatus> statusByChatId =
        statuses.stream()
            .collect(Collectors.toMap(
                LastMessageStatusProjection::getChatId,
                projection ->
                    LastMessageStatus.valueOf(
                        projection.getStatus())));

    return chatIds.stream()
        .map(chatId -> {

          LastMessageProjection projection =
              lastMessageByChatId.get(chatId.value());

          LastMessageResult lastMessage =
              projection == null
                  ? null
                  : lastMessagePersistenceMapper.toResult(projection);

          long unreadCount =
              unreadCountByChatId.getOrDefault(
                  chatId.value(),
                  0L);

          LastMessageStatus status =
              statusByChatId.getOrDefault(
                  chatId.value(),
                  LastMessageStatus.NONE);

          return new ChatListData(
              chatId,
              lastMessage,
              unreadCount,
              status);
        })
        .toList();
  }

  @Override
  public List<FileAttachment> findAttachmentsByChatId(ChatId chatId) {

    Objects.requireNonNull(chatId, "chatId");

    return messageJpaRepository.findAttachmentsByChatId(chatId.value()).stream()
        .map(fileAttachmentPersistenceMapper::toDomain)
        .toList();
  }

  @Override
  public Optional<MessageWithSender> findByIdAndChatId(MessageId messageId, ChatId chatId) {

    Objects.requireNonNull(messageId, "messageId");
    Objects.requireNonNull(chatId, "chatId");

    return messageJpaRepository
        .findByIdAndChatId(messageId.value(), chatId.value())
        .map(messagePersistenceMapper::toMessageWithSender);
  }

  @Override
  public List<MessageWithSender> findWithSenderByChatId(ChatId chatId, int limit) {

    Objects.requireNonNull(chatId, "chatId");

    return messageJpaRepository
        .findWithSenderByChatId(chatId.value(), PageRequest.of(0, limit))
        .stream()
        .map(messagePersistenceMapper::toMessageWithSender)
        .toList();
  }

  @Override
  public List<MessageWithSender> findWithSenderByChatIdAndCursor(
      ChatId chatId, Instant createdAt, MessageId id, int limit) {

    Objects.requireNonNull(chatId, "chatId");
    Objects.requireNonNull(createdAt, "createdAt");
    Objects.requireNonNull(id, "id");

    return messageJpaRepository
        .findWithSenderByChatIdAndCursor(
            chatId.value(), createdAt, id.value(), PageRequest.of(0, limit))
        .stream()
        .map(messagePersistenceMapper::toMessageWithSender)
        .toList();
  }

  @Override
  public int deleteByChatId(ChatId chatId) {

    Objects.requireNonNull(chatId, "chatId");

    return messageJpaRepository.deleteByChatId(chatId.value());
  }

  @Override
  public Message save(Message message) {

    Objects.requireNonNull(message, "message");

    ChatJpaEntity chat = chatJpaRepository.getReferenceById(message.getChatId().value());

    UserJpaEntity sender = userJpaRepository.getReferenceById(message.getSenderId().value());

    MessageJpaEntity entity = messagePersistenceMapper.toEntity(message, chat, sender);

    MessageJpaEntity saved = messageJpaRepository.save(entity);

    return messagePersistenceMapper.toDomain(saved);
  }

  @Override
  public void delete(Message message) {

    Objects.requireNonNull(message, "message");

    messageJpaRepository.deleteById(message.getId().value());
  }
}
