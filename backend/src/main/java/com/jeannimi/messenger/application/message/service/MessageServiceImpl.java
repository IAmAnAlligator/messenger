package com.jeannimi.messenger.application.message.service;

import com.jeannimi.messenger.application.chat.dto.ChatMemberReadResult;
import com.jeannimi.messenger.application.common.pagination.Cursor;
import com.jeannimi.messenger.application.common.pagination.CursorPageQuery;
import com.jeannimi.messenger.application.common.pagination.CursorPageResult;
import com.jeannimi.messenger.application.event.EventType;
import com.jeannimi.messenger.application.event.FileDeletionRequestedEvent;
import com.jeannimi.messenger.application.event.MessageCreatedEvent;
import com.jeannimi.messenger.application.event.MessageDeletedEvent;
import com.jeannimi.messenger.application.event.MessageReadEvent;
import com.jeannimi.messenger.application.exception.ForbiddenException;
import com.jeannimi.messenger.application.exception.NotFoundException;
import com.jeannimi.messenger.application.message.MessageApplicationConstants;
import com.jeannimi.messenger.application.message.command.FileUploadCommand;
import com.jeannimi.messenger.application.message.dto.FileAttachmentResult;
import com.jeannimi.messenger.application.message.dto.FileDownloadResult;
import com.jeannimi.messenger.application.message.dto.MessageResult;
import com.jeannimi.messenger.application.message.dto.MessageWithSender;
import com.jeannimi.messenger.application.message.dto.ReadResult;
import com.jeannimi.messenger.application.port.out.ChatMemberRepositoryPort;
import com.jeannimi.messenger.application.port.out.ChatRepositoryPort;
import com.jeannimi.messenger.application.port.out.EventPublisherPort;
import com.jeannimi.messenger.application.port.out.FileAttachmentRepositoryPort;
import com.jeannimi.messenger.application.port.out.FileStoragePort;
import com.jeannimi.messenger.application.port.out.MessageRepositoryPort;
import com.jeannimi.messenger.application.port.out.StoredFile;
import com.jeannimi.messenger.application.port.out.UserRepositoryPort;
import com.jeannimi.messenger.application.user.dto.UserResult;
import com.jeannimi.messenger.domain.chat.Chat;
import com.jeannimi.messenger.domain.chat.ChatMember;
import com.jeannimi.messenger.domain.exception.MessageError;
import com.jeannimi.messenger.domain.exception.MessageException;
import com.jeannimi.messenger.domain.message.FileAttachment;
import com.jeannimi.messenger.domain.message.FileAttachmentConstants;
import com.jeannimi.messenger.domain.message.Message;
import com.jeannimi.messenger.domain.user.User;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageServiceImpl implements MessageService {

  private final MessageRepositoryPort messageRepository;
  private final ChatRepositoryPort chatRepository;
  private final ChatMemberRepositoryPort chatMemberRepository;
  private final UserRepositoryPort userRepository;
  private final EventPublisherPort eventPublisher;
  private final FileStoragePort fileStoragePort;
  private final FileAttachmentRepositoryPort fileAttachmentRepository;

  @Override
  @Transactional(readOnly = true)
  public FileDownloadResult getFile(Long chatId, Long messageId, Long userId) {

    checkMembership(chatId, userId);

    MessageWithSender messageWithSender =
        messageRepository
            .findByIdAndChatId(messageId, chatId)
            .orElseThrow(() -> new NotFoundException("Message not found"));

    Message message = messageWithSender.message();

    FileAttachment attachment = message.getAttachment();

    if (attachment == null) {
      throw new NotFoundException("Message does not contain a file");
    }

    InputStream inputStream = fileStoragePort.load(attachment.getStorageFileName());

    return new FileDownloadResult(
        inputStream,
        attachment.getOriginalFileName(),
        attachment.getContentType(),
        attachment.getSize());
  }

  @Override
  @Transactional
  public MessageResult sendFile(Long chatId, Long senderId, FileUploadCommand file) {

    if (file == null) {
      throw new MessageException(MessageError.FILE_EMPTY, "File must not be empty");
    }

    Chat chat = getChatForSending(chatId, senderId);

    checkMembership(chatId, senderId);

    User sender = getUser(senderId);

    String originalFileName = sanitizeFileName(file.originalFileName());

    StoredFile storedFile;

    try (InputStream inputStream = file.inputStream()) {

      storedFile = fileStoragePort.store(inputStream, originalFileName);

    } catch (IOException e) {

      throw new MessageException(MessageError.FILE_STORAGE_FAILED, "Failed to read uploaded file");
    }

    String contentType = fileStoragePort.detectContentType(storedFile.storageFileName());

    if (contentType == null || contentType.isBlank()) {

      contentType = "application/octet-stream";
    }

    try {

      FileAttachment attachment =
          FileAttachment.create(
              originalFileName, storedFile.storageFileName(), contentType, file.size());

      Message message = Message.ofFile(chat.getId(), sender.getId(), attachment);

      Message savedMessage = messageRepository.save(message);

      chat.updateLastMessageTime();

      chatRepository.save(chat);

      /*
       * sender уже получен выше.
       * Повторный запрос UserRepository не нужен.
       */
      MessageResult result = toResult(savedMessage, sender);

      publishMessageCreated(result);

      return result;

    } catch (RuntimeException e) {

      try {

        fileStoragePort.delete(storedFile.storageFileName());

      } catch (RuntimeException cleanupException) {

        e.addSuppressed(cleanupException);
      }

      throw e;
    }
  }

  private String sanitizeFileName(String fileName) {

    if (fileName == null || fileName.isBlank()) {

      throw new MessageException(MessageError.FILE_NAME_INVALID, "File name must not be empty");
    }

    String sanitizedName = fileName.replace('\\', '/');

    int lastSeparator = sanitizedName.lastIndexOf('/');

    if (lastSeparator >= 0) {
      sanitizedName = sanitizedName.substring(lastSeparator + 1);
    }

    if (sanitizedName.isBlank()) {

      throw new MessageException(MessageError.FILE_NAME_INVALID, "File name must not be empty");
    }

    for (int i = 0; i < sanitizedName.length(); i++) {

      if (Character.isISOControl(sanitizedName.charAt(i))) {

        throw new MessageException(
            MessageError.FILE_NAME_INVALID, "File name contains invalid characters");
      }
    }

    if (sanitizedName.length() > FileAttachmentConstants.MAX_FILE_NAME_LENGTH) {

      throw new MessageException(MessageError.FILE_NAME_INVALID, "File name is too long");
    }

    return sanitizedName;
  }

  @Override
  @Transactional
  public MessageResult sendMessage(Long chatId, Long senderId, String content) {

    Chat chat = getChatForSending(chatId, senderId);

    checkMembership(chatId, senderId);

    User sender = getUser(senderId);

    Message message = Message.ofText(chat.getId(), sender.getId(), content);

    Message saved = messageRepository.save(message);

    chat.updateLastMessageTime();

    chatRepository.save(chat);

    /*
     * sender уже есть.
     * Поэтому не выполняем findById() повторно.
     */
    MessageResult result = toResult(saved, sender);

    publishMessageCreated(result);

    return result;
  }

  @Override
  @Transactional(readOnly = true)
  public CursorPageResult<MessageResult> getMessages(
      Long chatId, Long userId, CursorPageQuery query) {

    checkMembership(chatId, userId);

    int pageSize = Math.min(query.limit(), MessageApplicationConstants.MAX_MESSAGE_PAGE_SIZE);

    int fetchSize = pageSize + 1;

    List<MessageWithSender> messages =
        query.cursorTime() == null
            ? messageRepository.findWithSenderByChatId(chatId, fetchSize)
            : messageRepository.findWithSenderByChatIdAndCursor(
                chatId, query.cursorTime(), query.cursorId(), fetchSize);

    boolean hasMore = messages.size() > pageSize;

    if (hasMore) {

      messages = messages.subList(0, pageSize);
    }

    Cursor nextCursor = createNextCursor(messages, hasMore);

    return new CursorPageResult<>(
        messages.stream().map(this::toResult).toList(), nextCursor, hasMore);
  }

  private Cursor createNextCursor(List<MessageWithSender> messages, boolean hasMore) {

    if (!hasMore || messages.isEmpty()) {

      return null;
    }

    Message last = messages.get(messages.size() - 1).message();

    return new Cursor(last.getCreatedAt(), last.getId());
  }

  @Override
  @Transactional(readOnly = true)
  public MessageResult getMessage(Long chatId, Long messageId, Long userId) {

    checkMembership(chatId, userId);

    MessageWithSender messageWithSender =
        messageRepository
            .findByIdAndChatId(messageId, chatId)
            .orElseThrow(() -> new NotFoundException("Message not found"));

    return toResult(messageWithSender);
  }

  @Override
  @Transactional
  public ReadResult markAsRead(Long chatId, Long messageId, Long userId) {

    ChatMember chatMember =
        chatMemberRepository
            .findByChatIdAndUserId(chatId, userId)
            .orElseThrow(() -> new NotFoundException("Chat member not found"));

    MessageWithSender messageWithSender =
        messageRepository
            .findByIdAndChatId(messageId, chatId)
            .orElseThrow(() -> new NotFoundException("Message not found"));

    Message message = messageWithSender.message();

    if (message.getSenderId().equals(userId)) {

      return new ReadResult(
          new ChatMemberReadResult(userId, chatMember.getLastReadMessageId()), false);
    }

    int updated = chatMemberRepository.updateLastReadMessageId(chatId, userId, messageId);

    Long lastReadMessageId = updated > 0 ? messageId : chatMember.getLastReadMessageId();

    if (updated == 0) {

      return new ReadResult(new ChatMemberReadResult(userId, lastReadMessageId), false);
    }

    MessageReadEvent event =
        new MessageReadEvent(message.getId(), chatId, userId, Instant.now(), lastReadMessageId);

    eventPublisher.publish(EventType.MESSAGE_READ, String.valueOf(chatId), event);

    return new ReadResult(new ChatMemberReadResult(userId, lastReadMessageId), true);
  }

  @Override
  @Transactional
  public void deleteMessage(Long chatId, Long messageId, Long userId) {

    checkMembership(chatId, userId);

    MessageWithSender messageWithSender =
        messageRepository
            .findByIdAndChatId(messageId, chatId)
            .orElseThrow(() -> new NotFoundException("Message not found"));

    Message message = messageWithSender.message();

    if (!message.getSenderId().equals(userId)) {

      throw new ForbiddenException("Only sender can delete message");
    }

    FileAttachment attachment = message.getAttachment();

    Instant deletedAt = Instant.now();

    MessageDeletedEvent messageDeletedEvent =
        new MessageDeletedEvent(message.getId(), chatId, userId, deletedAt);

    messageRepository.delete(message);

    eventPublisher.publish(EventType.MESSAGE_DELETED, String.valueOf(chatId), messageDeletedEvent);

    publishFileDeletion(attachment);
  }

  @Override
  @Transactional
  public void deleteAllByChat(Long chatId) {

    List<FileAttachment> attachments = messageRepository.findAttachmentsByChatId(chatId);

    int deletedMessages = messageRepository.deleteByChatId(chatId);

    int deletedAttachments = 0;

    if (!attachments.isEmpty()) {

      List<UUID> attachmentIds = attachments.stream().map(FileAttachment::getId).toList();

      deletedAttachments = fileAttachmentRepository.deleteAllByIds(attachmentIds);

      for (FileAttachment attachment : attachments) {

        publishFileDeletion(attachment);
      }
    }

    log.info(
        "Deleted chat messages. chatId={}, messages={}, attachments={}",
        chatId,
        deletedMessages,
        deletedAttachments);
  }

  private void publishFileDeletion(FileAttachment attachment) {

    if (attachment == null) {
      return;
    }

    FileDeletionRequestedEvent event =
        new FileDeletionRequestedEvent(attachment.getStorageFileName());

    eventPublisher.publish(
        EventType.FILE_DELETION_REQUESTED, attachment.getStorageFileName(), event);
  }

  private void checkMembership(Long chatId, Long userId) {

    if (!chatMemberRepository.existsByChatIdAndUserId(chatId, userId)) {

      throw new ForbiddenException("You are not a member of this chat");
    }
  }

  private Chat getChatForSending(Long chatId, Long userId) {

    Chat chat =
        chatRepository.findById(chatId).orElseThrow(() -> new NotFoundException("Chat not found"));

    checkMembership(chatId, userId);

    return chat;
  }

  private User getUser(Long userId) {

    return userRepository
        .findById(userId)
        .orElseThrow(() -> new NotFoundException("User not found"));
  }

  private void publishMessageCreated(MessageResult result) {

    MessageCreatedEvent event = new MessageCreatedEvent(result);

    eventPublisher.publish(EventType.MESSAGE_CREATED, String.valueOf(result.chatId()), event);
  }

  /** Используется для сообщений, которые уже были загружены вместе с sender через JOIN FETCH. */
  private MessageResult toResult(MessageWithSender messageWithSender) {

    Message message = messageWithSender.message();

    User sender = messageWithSender.sender();

    return toResult(message, sender);
  }

  /**
   * Используется при отправке сообщения, когда sender уже был загружен в sendMessage()/sendFile().
   */
  private MessageResult toResult(Message message, User sender) {

    FileAttachment attachment = message.getAttachment();

    FileAttachmentResult attachmentResult =
        attachment == null
            ? null
            : new FileAttachmentResult(
                attachment.getId(),
                attachment.getOriginalFileName(),
                attachment.getContentType(),
                attachment.getSize());

    UserResult senderResult =
        new UserResult(sender.getId(), sender.getUsername().getValue(), sender.getRole());

    return new MessageResult(
        message.getId(),
        message.getChatId(),
        senderResult,
        message.getContent(),
        message.getCreatedAt(),
        attachmentResult);
  }
}
