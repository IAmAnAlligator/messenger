package com.jeannimi.messenger.message.service;

import com.jeannimi.messenger.chat.entity.Chat;
import com.jeannimi.messenger.chat.repository.ChatMemberRepository;
import com.jeannimi.messenger.chat.repository.ChatRepository;
import com.jeannimi.messenger.common.exception_handling.BadRequestException;
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
import com.jeannimi.messenger.message.entity.MessageStatus;
import com.jeannimi.messenger.message.repository.MessageRepository;
import com.jeannimi.messenger.message.storage.FileStorageService;
import com.jeannimi.messenger.message.storage.StoredFile;
import com.jeannimi.messenger.outbox.publisher.EventPublisher;
import com.jeannimi.messenger.user.entity.User;
import com.jeannimi.messenger.user.repository.UserRepository;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

  private final MessageRepository messageRepository;
  private final ChatRepository chatRepository;
  private final ChatMemberRepository chatMemberRepository;
  private final UserRepository userRepository;
  private final EventPublisher eventPublisher;
  private final FileStorageService fileStorageService;


  @Override
  @Transactional(readOnly = true)
  public FileDownload getFile(
      Long chatId,
      Long messageId,
      Long userId) {

    // Проверяем, что пользователь участник чата
    checkMembership(chatId, userId);

    // Получаем сообщение
    Message message =
        messageRepository
            .findByIdAndChatId(messageId, chatId)
            .orElseThrow(
                () -> new NotFoundException(
                    "Message not found"));

    // Получаем attachment
    FileAttachment attachment =
        message.getAttachment();

    if (attachment == null) {
      throw new NotFoundException(
          "Message does not contain a file");
    }

    // Загружаем физический файл
    InputStream inputStream =
        fileStorageService.load(
            attachment.getStorageFileName());

    return new FileDownload(
        inputStream,
        attachment.getOriginalFileName(),
        attachment.getContentType(),
        attachment.getSize());
  }

  @Override
  @Transactional
  public MessageDto sendFile(
      Long chatId,
      Long senderId,
      FileUpload file) {

    // 1. Проверка HTTP-входа

    if (file == null || file.size() <= 0) {
      throw new MessageException(
          MessageError.FILE_EMPTY,
          "File must not be empty");
    }

    // 2. Проверяем чат

    Chat chat =
        chatRepository
            .findById(chatId)
            .orElseThrow(
                () -> new NotFoundException(
                    "Chat not found"));

    // 3. Проверяем участника

    checkMembership(chatId, senderId);

    // 4. Получаем отправителя

    User sender =
        userRepository
            .findById(senderId)
            .orElseThrow(
                () -> new NotFoundException(
                    "User not found"));

    // 5. Имя файла

    String originalFileName =
        file.originalFileName();

    if (originalFileName == null) {
      throw new MessageException(
          MessageError.FILE_NAME_INVALID,
          "File name must not be empty");
    }

    originalFileName =
        Paths.get(originalFileName)
            .getFileName()
            .toString();

    // 6. Content-Type

    String contentType =
        file.contentType();

    if (contentType == null
        || contentType.isBlank()) {

      contentType =
          "application/octet-stream";
    }

    // 7. Сохраняем физический файл

    StoredFile storedFile;

    try (InputStream inputStream = file.inputStream()) {

      storedFile =
          fileStorageService.store(
              inputStream,
              originalFileName);

    } catch (IOException e) {

      throw new MessageException(
          MessageError.FILE_STORAGE_FAILED,
          "Failed to read uploaded file");
    }

    // 8. Создаём attachment + message
    //
    // Если что-то после физического сохранения
    // упадёт — удаляем физический файл.

    try {

      FileAttachment attachment =
          FileAttachment.create(
              originalFileName,
              storedFile.storageFileName(),
              contentType,
              file.size(),
              storedFile.storagePath());

      Message message =
          Message.ofFile(
              chat,
              sender,
              attachment);

      Message savedMessage =
          messageRepository.save(message);

      return toDto(savedMessage);

    } catch (RuntimeException e) {

      try {

        fileStorageService.delete(
            storedFile.storageFileName());

      } catch (FileStorageException cleanupException) {

        e.addSuppressed(cleanupException);
      }

      throw e;
    }
  }

  // =========================
  // SEND
  // =========================

  @Override
  @Transactional
  public MessageDto sendMessage(Long chatId, Long senderId, String content) {

    // 1. Проверка: чат существует
    Chat chat =
        chatRepository.findById(chatId).orElseThrow(() -> new NotFoundException("Chat not found"));

    // 2. Проверка: пользователь участник чата
    checkMembership(chatId, senderId);

    // 3. Получаем sender (можно через getReference для оптимизации)
    User sender =
        userRepository
            .findById(senderId)
            .orElseThrow(() -> new NotFoundException("User not found"));

    // 4. Создаём сообщение
    Message message = Message.ofText(chat, sender, content);

    // 5. Сохраняем
    Message saved = messageRepository.save(message);

    chat.updateLastMessageTime();

    MessageSentEvent messageSentEvent = MessageSentEvent.from(saved);

    eventPublisher.publish(
        KafkaTopics.CHAT_MESSAGES,
        EventType.MESSAGE_CREATED,
        String.valueOf(chatId),
        messageSentEvent);

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

    checkMembership(chatId, userId);

    Message message =
        messageRepository
            .findByIdAndChatId(messageId, chatId)
            .orElseThrow(() -> new NotFoundException("Message not found"));

    if (message.getSender().getId().equals(userId)) {
      throw new BadRequestException("Cannot mark your own message as read");
    }

    boolean changed = false;

    if (message.getStatus() != MessageStatus.READ) {

      message.markRead();

      MessageReadEvent messageReadEvent =
          new MessageReadEvent(message.getId(), chatId, userId, Instant.now());

      eventPublisher.publish(
          KafkaTopics.CHAT_READ, EventType.MESSAGE_READ, String.valueOf(chatId), messageReadEvent);

      changed = true;
    }

    return new ReadResult(toDto(message), changed);
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

    messageRepository.delete(message);

    eventPublisher.publish(
        KafkaTopics.CHAT_MESSAGE_DELETED,
        EventType.MESSAGE_DELETED,
        String.valueOf(chatId),
        messageDeletedEvent);
  }

  @Override
  @Transactional
  public void deleteAllByChat(Long chatId) {
    messageRepository.deleteByChatId(chatId);
  }

  private void checkMembership(Long chatId, Long userId) {
    if (!chatMemberRepository.existsByChatIdAndUserId(chatId, userId)) {
      throw new ForbiddenException("You are not a member of this chat");
    }
  }

  private MessageDto toDto(Message m) {
    return MessageDto.toDto(m);
  }
}
