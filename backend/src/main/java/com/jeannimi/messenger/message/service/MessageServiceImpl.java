package com.jeannimi.messenger.message.service;

import com.jeannimi.messenger.message.dto.MessageDto;
import com.jeannimi.messenger.message.dto.ReadResult;
import com.jeannimi.messenger.kafka.event.MessageDeletedEvent;
import com.jeannimi.messenger.kafka.event.MessageReadEvent;
import com.jeannimi.messenger.kafka.event.MessageSentEvent;
import com.jeannimi.messenger.chat.entity.Chat;
import com.jeannimi.messenger.common.constants.EventType;
import com.jeannimi.messenger.message.entity.Message;
import com.jeannimi.messenger.message.entity.MessageStatus;
import com.jeannimi.messenger.user.entity.User;
import com.jeannimi.messenger.common.exception_handling.BadRequestException;
import com.jeannimi.messenger.common.exception_handling.ForbiddenException;
import com.jeannimi.messenger.common.exception_handling.NotFoundException;
import com.jeannimi.messenger.kafka.KafkaTopics;
import com.jeannimi.messenger.outbox.publisher.EventPublisher;
import com.jeannimi.messenger.chat.repository.ChatMemberRepository;
import com.jeannimi.messenger.chat.repository.ChatRepository;
import com.jeannimi.messenger.message.repository.MessageRepository;
import com.jeannimi.messenger.user.repository.UserRepository;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MessageServiceImpl implements MessageService {

  private final MessageRepository messageRepository;
  private final ChatRepository chatRepository;
  private final ChatMemberRepository chatMemberRepository;
  private final UserRepository userRepository;
  private final EventPublisher eventPublisher;

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
    Message message = Message.of(chat, sender, content);

    // 5. Сохраняем
    Message saved = messageRepository.save(message);

    chat.updateLastMessageTime();

    MessageSentEvent messageSentEvent =
        MessageSentEvent.from(saved);

    eventPublisher.publish(KafkaTopics.CHAT_MESSAGES,
        EventType.MESSAGE_SENT,
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

      MessageReadEvent messageReadEvent = new MessageReadEvent(message.getId(),
          chatId,
          userId,
          Instant.now());

      eventPublisher.publish(KafkaTopics.CHAT_READ,
          EventType.MESSAGE_READ,
          String.valueOf(chatId),
          messageReadEvent);

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
        new MessageDeletedEvent(
            message.getId(),
            chatId,
            userId,
            Instant.now()
        );

    messageRepository.delete(message);

    eventPublisher.publish(KafkaTopics.CHAT_MESSAGE_DELETED,
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
