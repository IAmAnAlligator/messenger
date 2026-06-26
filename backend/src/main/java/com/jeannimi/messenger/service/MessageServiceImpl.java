package com.jeannimi.messenger.service;

import com.jeannimi.messenger.dto.MessageDto;
import com.jeannimi.messenger.dto.ReadResult;
import com.jeannimi.messenger.dto.UserDto;
import com.jeannimi.messenger.entity.Chat;
import com.jeannimi.messenger.entity.Message;
import com.jeannimi.messenger.entity.MessageStatus;
import com.jeannimi.messenger.entity.OutboxEvent;
import com.jeannimi.messenger.entity.OutboxStatus;
import com.jeannimi.messenger.entity.User;
import com.jeannimi.messenger.exception_handling.BadRequestException;
import com.jeannimi.messenger.exception_handling.ForbiddenException;
import com.jeannimi.messenger.exception_handling.NotFoundException;
import com.jeannimi.messenger.repository.ChatMemberRepository;
import com.jeannimi.messenger.repository.ChatRepository;
import com.jeannimi.messenger.repository.MessageRepository;
import com.jeannimi.messenger.repository.OutboxRepository;
import com.jeannimi.messenger.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.ObjectMapper;

@Service
@RequiredArgsConstructor
@Transactional
public class MessageServiceImpl implements MessageService {

  private final MessageRepository messageRepository;
  private final ChatRepository chatRepository;
  private final ChatMemberRepository chatMemberRepository;
  private final UserRepository userRepository;
  private final OutboxRepository outboxRepository;
  private final ObjectMapper objectMapper;

  // =========================
  // SEND
  // =========================

  @Override
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

    if (content == null || content.isBlank()) {
      throw new BadRequestException("Message content cannot be empty");
    }

    // 4. Создаём сообщение
    Message message = Message.of(chat, sender, content);

    // 5. Сохраняем
    Message saved = messageRepository.save(message);

    chat.updateLastMessageTime();

    MessageDto dto = toDto(saved);

    OutboxEvent event =
        OutboxEvent.builder()
            .topic("chat.messages")
            .payload(objectMapper.writeValueAsString(dto))
            .status(OutboxStatus.NEW)
            .createdAt(LocalDateTime.now())
            .build();

    outboxRepository.save(event);

    // 6. Возвращаем DTO
    return dto;
  }

  // =========================
  // GET LIST (cursor pagination)
  // =========================

  @Override
  @Transactional(readOnly = true)
  public List<MessageDto> getMessages(Long chatId, Long userId, Long cursor) {

    checkMembership(chatId, userId);

    PageRequest pageable = PageRequest.of(0, 50);

    List<Message> messages =
        (cursor == null)
            ? messageRepository.findWithSenderByChatId(chatId, pageable)
            : messageRepository.findWithSenderByChatIdAndCursor(chatId, cursor, pageable);

    return messages.stream().map(this::toDto).toList();
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

      changed = true;
    }

    return new ReadResult(toDto(message), changed);
  }

  // =========================
  // DELETE
  // =========================

  @Override
  public void deleteMessage(Long chatId, Long messageId, Long userId) {

    checkMembership(chatId, userId);

    Message message =
        messageRepository
            .findByIdAndChatId(messageId, chatId)
            .orElseThrow(() -> new NotFoundException("Message not found"));

    if (!message.getSender().getId().equals(userId)) {
      throw new ForbiddenException("Only sender can delete message");
    }

    messageRepository.delete(message);
  }

  private void checkMembership(Long chatId, Long userId) {
    if (!chatMemberRepository.existsByChatIdAndUserId(chatId, userId)) {
      throw new ForbiddenException("You are not a member of this chat");
    }
  }

  private MessageDto toDto(Message m) {
    return MessageDto.builder()
        .id(m.getId())
        .chatId(m.getChat().getId())
        .sender(
            new UserDto(
                m.getSender().getId(),
                m.getSender().getUsername().getValue(),
                m.getSender().getRole()))
        .content(m.getContent())
        .createdAt(m.getCreatedAt())
        .status(m.getStatus())
        .build();
  }
}
