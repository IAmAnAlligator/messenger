package com.jeannimi.messenger.message.service;

import com.jeannimi.messenger.chat.dto.ChatMemberReadDto;
import com.jeannimi.messenger.chat.entity.Chat;
import com.jeannimi.messenger.chat.entity.ChatMember;
import com.jeannimi.messenger.chat.repository.ChatMemberRepository;
import com.jeannimi.messenger.chat.repository.ChatRepository;
import com.jeannimi.messenger.common.exception_handling.FileStorageException;
import com.jeannimi.messenger.common.exception_handling.ForbiddenException;
import com.jeannimi.messenger.common.exception_handling.MessageError;
import com.jeannimi.messenger.common.exception_handling.MessageException;
import com.jeannimi.messenger.common.exception_handling.NotFoundException;
import com.jeannimi.messenger.common.pagination.CursorDto;
import com.jeannimi.messenger.common.pagination.CursorPageRequest;
import com.jeannimi.messenger.common.pagination.CursorPageResponse;
import com.jeannimi.messenger.kafka.KafkaTopics;
import com.jeannimi.messenger.kafka.event.EventType;
import com.jeannimi.messenger.kafka.event.FileDeletionRequestedEvent;
import com.jeannimi.messenger.kafka.event.MessageDeletedEvent;
import com.jeannimi.messenger.kafka.event.MessageReadEvent;
import com.jeannimi.messenger.kafka.event.MessageSentEvent;
import com.jeannimi.messenger.message.MessageConstants;
import com.jeannimi.messenger.message.dto.FileDownload;
import com.jeannimi.messenger.message.dto.FileUpload;
import com.jeannimi.messenger.message.dto.MessageDto;
import com.jeannimi.messenger.message.dto.ReadResult;
import com.jeannimi.messenger.message.entity.FileAttachment;
import com.jeannimi.messenger.message.entity.Message;
import com.jeannimi.messenger.message.repository.FileAttachmentRepository;
import com.jeannimi.messenger.message.repository.MessageRepository;
import com.jeannimi.messenger.message.storage.FileStorageService;
import com.jeannimi.messenger.message.storage.StoredFile;
import com.jeannimi.messenger.outbox.publisher.EventPublisher;
import com.jeannimi.messenger.user.entity.User;
import com.jeannimi.messenger.user.repository.UserRepository;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class MessageServiceImpl implements MessageService {

  private final MessageRepository messageRepository;
  private final ChatRepository chatRepository;
  private final ChatMemberRepository chatMemberRepository;
  private final UserRepository userRepository;
  private final EventPublisher eventPublisher;
  private final FileStorageService fileStorageService;
  private final FileAttachmentRepository fileAttachmentRepository;

  @Override
  @Transactional(readOnly = true)
  public FileDownload getFile(Long chatId, Long messageId, Long userId) {

    // Проверяем, что пользователь участник чата
    checkMembership(chatId, userId);

    // Получаем сообщение
    Message message =
        messageRepository
            .findByIdAndChatId(messageId, chatId)
            .orElseThrow(() -> new NotFoundException("Message not found"));

    // Получаем attachment
    FileAttachment attachment = message.getAttachment();

    if (attachment == null) {
      throw new NotFoundException("Message does not contain a file");
    }

    // Загружаем физический файл
    InputStream inputStream = fileStorageService.load(attachment.getStorageFileName());

    return new FileDownload(
        inputStream,
        attachment.getOriginalFileName(),
        attachment.getContentType(),
        attachment.getSize());
  }

  @Override
  @Transactional
  public MessageDto sendFile(Long chatId, Long senderId, FileUpload file) {

    // =========================
    // 1. Проверка входных данных
    // =========================

    if (file == null || file.size() <= 0) {
      throw new MessageException(MessageError.FILE_EMPTY, "File must not be empty");
    }

    if (file.size() > MessageConstants.MAX_FILE_SIZE_BYTES) {
      throw new MessageException(
          MessageError.FILE_TOO_LARGE, "File size exceeds the maximum allowed size");
    }

    // =========================
    // 2. Проверяем чат
    // =========================

    Chat chat = getChatForSending(chatId, senderId);

    // =========================
    // 3. Проверяем участника
    // =========================

    checkMembership(chatId, senderId);

    // =========================
    // 4. Получаем отправителя
    // =========================

    User sender = getUser(senderId);

    // =========================
    // 5. Проверяем имя файла
    // =========================

    String originalFileName = sanitizeFileName(file.originalFileName());

    // =========================
    // 6. Сохраняем файл
    // =========================

    StoredFile storedFile;

    try (InputStream inputStream = file.inputStream()) {

      storedFile = fileStorageService.store(inputStream, originalFileName);

    } catch (IOException e) {

      throw new MessageException(MessageError.FILE_STORAGE_FAILED, "Failed to read uploaded file");
    }

    // =========================
    // 7. Определяем MIME type
    // =========================

    String contentType;

    try {

      contentType = Files.probeContentType(Path.of(storedFile.storagePath()));

    } catch (IOException e) {

      contentType = null;
    }

    if (contentType == null || contentType.isBlank()) {

      contentType = "application/octet-stream";
    }

    // =========================
    // 8. Создаём attachment
    //    и message
    // =========================

    try {

      FileAttachment attachment =
          FileAttachment.create(
              originalFileName,
              storedFile.storageFileName(),
              contentType,
              file.size(),
              storedFile.storagePath());

      Message message = Message.ofFile(chat, sender, attachment);

      Message savedMessage = messageRepository.save(message);

      chat.updateLastMessageTime();

      publishMessageCreated(savedMessage);

      return toDto(savedMessage);

    } catch (RuntimeException e) {

      /*
       * Если сохранение в БД или создание события
       * завершилось ошибкой, физический файл больше
       * не должен оставаться в storage.
       */
      try {

        fileStorageService.delete(storedFile.storageFileName());

      } catch (FileStorageException cleanupException) {

        e.addSuppressed(cleanupException);
      }

      throw e;
    }
  }

  private String sanitizeFileName(String fileName) {

    if (fileName == null || fileName.isBlank()) {
      throw new MessageException(MessageError.FILE_NAME_INVALID, "File name must not be empty");
    }

    /*
     * Убираем путь, переданный клиентом.
     *
     * Например:
     *
     * ../../secret.txt
     * C:\Users\User\secret.txt
     *
     * превращается в:
     *
     * secret.txt
     */
    String sanitizedName = Paths.get(fileName).getFileName().toString();

    if (sanitizedName.isBlank()) {
      throw new MessageException(MessageError.FILE_NAME_INVALID, "File name must not be empty");
    }

    /*
     * Дополнительная защита от управляющих символов.
     */
    for (int i = 0; i < sanitizedName.length(); i++) {

      if (Character.isISOControl(sanitizedName.charAt(i))) {

        throw new MessageException(
            MessageError.FILE_NAME_INVALID, "File name contains invalid characters");
      }
    }

    /*
     * Не разрешаем слишком длинные имена.
     */
    if (sanitizedName.length() > 255) {
      throw new MessageException(MessageError.FILE_NAME_INVALID, "File name is too long");
    }

    return sanitizedName;
  }

  // =========================
  // SEND
  // =========================

  @Override
  @Transactional
  public MessageDto sendMessage(Long chatId, Long senderId, String content) {

    // 1. Проверка: чат существует
    Chat chat = getChatForSending(chatId, senderId);

    // 2. Проверка: пользователь участник чата
    checkMembership(chatId, senderId);

    // 3. Получаем sender (можно через getReference для оптимизации)
    User sender = getUser(senderId);

    // 4. Создаём сообщение
    Message message = Message.ofText(chat, sender, content);

    // 5. Сохраняем
    Message saved = messageRepository.save(message);

    chat.updateLastMessageTime();

    publishMessageCreated(message);

    // 6. Возвращаем DTO
    return toDto(saved);
  }

  // =========================
  // GET LIST (cursor pagination)
  // =========================

  @Override
  @Transactional(readOnly = true)
  public CursorPageResponse<MessageDto, CursorDto> getMessages(
      Long chatId, Long userId, CursorPageRequest request) {

    checkMembership(chatId, userId);

    int pageSize = Math.min(request.limit(), MessageConstants.MAX_MESSAGE_PAGE_SIZE);

    Pageable pageable = PageRequest.of(0, pageSize + 1);

    List<Message> messages =
        request.cursorTime() == null
            ? messageRepository.findWithSenderByChatId(chatId, pageable)
            : messageRepository.findWithSenderByChatIdAndCursor(
                chatId, request.cursorTime(), request.cursorId(), pageable);

    boolean hasMore = messages.size() > pageSize;

    if (hasMore) {
      messages = messages.subList(0, pageSize);
    }

    CursorDto nextCursor = createNextCursor(messages, hasMore);

    return new CursorPageResponse<>(
        messages.stream().map(this::toDto).toList(), nextCursor, hasMore);
  }

  private CursorDto createNextCursor(List<Message> messages, boolean hasMore) {

    if (!hasMore || messages.isEmpty()) {
      return null;
    }

    Message last = messages.get(messages.size() - 1);

    return new CursorDto(last.getCreatedAt(), last.getId());
  }

  // =========================
  // GET ONE
  // =========================

  @Override
  @Transactional(readOnly = true)
  public MessageDto getMessage(Long chatId, Long messageId, Long userId) {

    checkMembership(chatId, userId);

    Message message =
        messageRepository
            .findByIdAndChatId(messageId, chatId)
            .orElseThrow(() -> new NotFoundException("Message not found"));

    return toDto(message);
  }

  // =========================
  // MARK AS READ
  // =========================

  @Override
  @Transactional
  public ReadResult markAsRead(Long chatId, Long messageId, Long userId) {

    ChatMember chatMember =
        chatMemberRepository
            .findByChatIdAndUserId(chatId, userId)
            .orElseThrow(() -> new NotFoundException("Chat member not found"));

    Message message =
        messageRepository
            .findByIdAndChatId(messageId, chatId)
            .orElseThrow(() -> new NotFoundException("Message not found"));


    /*
     * Пользователь не может пометить
     * собственное сообщение прочитанным.
     */
    if (message.getSender().getId().equals(userId)) {

      return new ReadResult(
          new ChatMemberReadDto(
              userId,
              chatMember.getLastReadMessageId()),
          false);
    }

    int updated =
        chatMemberRepository.updateLastReadMessageId(
            chatId,
            userId,
            messageId);

    Long lastReadMessageId =
        updated > 0
            ? messageId
            : chatMember.getLastReadMessageId();

    if (updated == 0) {
      return new ReadResult(
          new ChatMemberReadDto(
              userId,
              lastReadMessageId),
          false);
    }

    MessageReadEvent event =
        new MessageReadEvent(
            message.getId(),
            chatId,
            userId,
            Instant.now(),
            lastReadMessageId);

    eventPublisher.publish(
        KafkaTopics.CHAT_READ,
        EventType.MESSAGE_READ,
        String.valueOf(chatId),
        event);

    return new ReadResult(
        new ChatMemberReadDto(
            userId,
            lastReadMessageId),
        true);
  }

  // =========================
  // DELETE
  // =========================

  @Override
  @Transactional
  public void deleteMessage(Long chatId, Long messageId, Long userId) {

    checkMembership(chatId, userId);

    Message message =
        messageRepository
            .findByIdAndChatId(messageId, chatId)
            .orElseThrow(() -> new NotFoundException("Message not found"));

    if (!message.getSender().getId().equals(userId)) {
      throw new ForbiddenException("Only sender can delete message");
    }

    MessageDeletedEvent messageDeletedEvent =
        new MessageDeletedEvent(message.getId(), chatId, userId, Instant.now());

    FileAttachment attachment = message.getAttachment();

    messageRepository.delete(message);

    publishFileDeletion(attachment);

    eventPublisher.publish(
        KafkaTopics.CHAT_MESSAGE_DELETED,
        EventType.MESSAGE_DELETED,
        String.valueOf(chatId),
        messageDeletedEvent);
  }

  @Override
  @Transactional
  public void deleteAllByChat(Long chatId) {

    List<FileAttachment> attachments = messageRepository.findAttachmentsByChatId(chatId);

    int deletedMessages = messageRepository.deleteByChatId(chatId);

    int deletedAttachments = 0;

    if (!attachments.isEmpty()) {

      List<UUID> attachmentIds = attachments.stream().map(FileAttachment::getId).toList();

      fileAttachmentRepository.deleteAllByIds(attachmentIds);

      deletedAttachments = attachmentIds.size();

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

    if (attachment != null) {

      FileDeletionRequestedEvent event =
          new FileDeletionRequestedEvent(attachment.getStorageFileName());

      eventPublisher.publish(
          KafkaTopics.FILE_DELETE,
          EventType.FILE_DELETION_REQUESTED,
          attachment.getStorageFileName(),
          event);
    }
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

  private void publishMessageCreated(Message message) {
    MessageSentEvent event = MessageSentEvent.from(message);

    eventPublisher.publish(
        KafkaTopics.CHAT_MESSAGES,
        EventType.MESSAGE_CREATED,
        String.valueOf(message.getChat().getId()),
        event);
  }

  private MessageDto toDto(Message m) {
    return MessageDto.toDto(m);
  }
}
