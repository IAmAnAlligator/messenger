package com.jeannimi.messenger.chat.service;

import static com.jeannimi.messenger.chat.entity.Chat.buildPrivateKey;

import com.jeannimi.messenger.chat.dto.ChatCreateRequest;
import com.jeannimi.messenger.chat.dto.ChatDto;
import com.jeannimi.messenger.chat.dto.ChatMemberDto;
import com.jeannimi.messenger.kafka.event.ChatCreatedEvent;
import com.jeannimi.messenger.kafka.event.ChatDeletedEvent;
import com.jeannimi.messenger.kafka.event.ChatMemberAddedEvent;
import com.jeannimi.messenger.kafka.event.ChatMemberLeftEvent;
import com.jeannimi.messenger.kafka.event.ChatMemberRemovedEvent;
import com.jeannimi.messenger.kafka.event.ChatRenamedEvent;
import com.jeannimi.messenger.chat.entity.Chat;
import com.jeannimi.messenger.kafka.event.EventType;
import com.jeannimi.messenger.user.entity.User;
import com.jeannimi.messenger.common.exception_handling.BadRequestException;
import com.jeannimi.messenger.common.exception_handling.ConflictException;
import com.jeannimi.messenger.common.exception_handling.ForbiddenException;
import com.jeannimi.messenger.common.exception_handling.NotFoundException;
import com.jeannimi.messenger.kafka.KafkaTopics;
import com.jeannimi.messenger.outbox.publisher.EventPublisher;
import com.jeannimi.messenger.chat.repository.ChatMemberRepository;
import com.jeannimi.messenger.chat.repository.ChatRepository;
import com.jeannimi.messenger.user.repository.UserRepository;
import com.jeannimi.messenger.message.service.MessageService;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

  private final ChatRepository chatRepository;
  private final UserRepository userRepository;
  private final ChatMemberRepository chatMemberRepository;
  private final MessageService messageService;
  private final EventPublisher eventPublisher;

  // =========================
  // CREATE CHAT
  // =========================

  @Override
  @Transactional
  public ChatDto createChat(ChatCreateRequest request, Long currentUserId) {

    User creator = loadUser(currentUserId);

    return switch (request.type()) {
      case PRIVATE -> createPrivateChat(request, creator);
      case GROUP -> createGroupChat(request, creator);
      default -> throw new BadRequestException("Unsupported chat type");
    };
  }

  private ChatDto createPrivateChat(ChatCreateRequest request, User creator) {

    if (request.memberIds() == null || request.memberIds().size() != 1) {
      throw new BadRequestException("Private chat must have exactly one member");
    }

    Long otherUserId = request.memberIds().get(0);

    String key = buildPrivateKey(creator.getId(), otherUserId);

    if (chatRepository.findByPrivateKey(key).isPresent()) {
      throw new ConflictException("Private chat already exists");
    }

    User otherUser = loadUser(otherUserId);

    Chat chat = Chat.createPrivate(creator, otherUser);

    return savePrivateChat(key, chat);
  }

  private ChatDto savePrivateChat(String key, Chat chat) {

    try {
      Chat saved = chatRepository.save(chat);

      ChatCreatedEvent chatCreatedEvent = new ChatCreatedEvent(UUID.randomUUID(),
          saved.getId(),
          saved.getName(),
          saved.getType(),
          saved.getMembers().stream().map(m -> m.getUser().getId()).toList());

      eventPublisher.publish( KafkaTopics.CHAT_EVENTS,
          EventType.CHAT_CREATED,
          String.valueOf(saved.getId()),
          chatCreatedEvent);

      return toDto(saved);

    } catch (DataIntegrityViolationException e) {
      return toDto(
          chatRepository
              .findByPrivateKey(key)
              .orElseThrow(() -> new ConflictException("Private chat already exists")));
    }
  }

  private ChatDto createGroupChat(ChatCreateRequest request, User creator) {

    Set<Long> uniqueIds =
        request.memberIds() == null ? new HashSet<>() : new HashSet<>(request.memberIds());

    uniqueIds.remove(creator.getId());

    List<User> users = userRepository.findAllById(uniqueIds);

    if (users.size() != uniqueIds.size()) {
      throw new NotFoundException("One or more users not found");
    }

    Chat chat = Chat.createGroup(request.name(), creator, users);

    Chat saved = chatRepository.save(chat);

    ChatCreatedEvent chatCreatedEvent = new ChatCreatedEvent(UUID.randomUUID(),
        saved.getId(),
        saved.getName(),
        saved.getType(),
        saved.getMembers().stream().map(m -> m.getUser().getId()).toList());

    eventPublisher.publish(KafkaTopics.CHAT_EVENTS,
        EventType.CHAT_CREATED,
        String.valueOf(saved.getId()),
        chatCreatedEvent);

    return toDto(saved);
  }

  // =========================
  // GET USER CHATS
  // =========================

  @Override
  @Transactional(readOnly = true)
  public List<ChatDto> getUserChats(Long userId) {
    return chatMemberRepository.findChatsWithMembersByUserId(userId).stream()
        .map(this::toDto)
        .toList();
  }

  // =========================
  // GET CHAT
  // =========================

  @Override
  @Transactional(readOnly = true)
  public ChatDto getChat(Long chatId, Long userId) {

    Chat chat = loadChat(chatId);

    if (!chat.hasMember(userId)) {
      throw new ForbiddenException("Access denied");
    }

    return toDto(chat);
  }

  // =========================
  // ADD MEMBER
  // =========================

  @Override
  @Transactional
  public void addMember(Long chatId, Long userId, Long currentUserId) {

    Chat chat = loadChat(chatId);

    User user = loadUser(userId);

    chat.addMember(user, currentUserId);

    ChatMemberAddedEvent chatMemberAddedEvent = new ChatMemberAddedEvent(UUID.randomUUID(),
        chat.getId(),
        user.getId(),
        user.getUsername().getValue());

    eventPublisher.publish(KafkaTopics.CHAT_EVENTS,
        EventType.CHAT_MEMBER_ADDED,
        String.valueOf(chat.getId()),
        chatMemberAddedEvent);

  }

  // =========================
  // REMOVE MEMBER
  // =========================

  @Override
  @Transactional
  public void removeMember(Long chatId, Long userId, Long currentUserId) {

    Chat chat = loadChat(chatId);

    chat.removeMember(userId, currentUserId);

    ChatMemberRemovedEvent chatMemberRemovedEvent =
        new ChatMemberRemovedEvent(UUID.randomUUID(), chat.getId(), userId);

    eventPublisher.publish(KafkaTopics.CHAT_EVENTS,
        EventType.CHAT_MEMBER_REMOVED,
        String.valueOf(chat.getId()),
        chatMemberRemovedEvent);

  }

  @Override
  @Transactional
  public void deleteChat(Long chatId, Long currentUserId) {

    Chat chat = loadChat(chatId);

    chat.ensureCanDelete(currentUserId);

    Long deletedChatId = chat.getId();

    messageService.deleteAllByChat(chatId);

    ChatDeletedEvent chatDeletedEvent = new ChatDeletedEvent(UUID.randomUUID(), deletedChatId);

    eventPublisher.publish(KafkaTopics.CHAT_EVENTS,
        EventType.CHAT_DELETED,
        String.valueOf(deletedChatId),
chatDeletedEvent);

    chatRepository.delete(chat);
  }

  @Override
  @Transactional
  public void leaveChat(Long chatId, Long currentUserId) {

    Chat chat = loadChat(chatId);

    chat.leaveChat(currentUserId);

    ChatMemberLeftEvent chatMemberLeftEvent = new ChatMemberLeftEvent(UUID.randomUUID(), chat.getId(), currentUserId);

    eventPublisher.publish(KafkaTopics.CHAT_EVENTS,
        EventType.CHAT_MEMBER_LEFT,
        String.valueOf(chat.getId()),
        chatMemberLeftEvent);

  }

  @Override
  @Transactional(readOnly = true)
  public List<ChatMemberDto> getMembers(Long chatId, Long currentUserId) {

    Chat chat = loadChat(chatId);

    if (!chat.hasMember(currentUserId)) {
      throw new ForbiddenException("Access denied");
    }

    return chat.getMembers().stream().map(ChatMemberDto::toDto).toList();
  }

  @Override
  @Transactional
  public void renameChat(Long chatId, String chatName, Long currentUserId) {

    Chat chat = loadChat(chatId);

    String oldName = chat.getName();

    chat.renameChat(currentUserId, chatName);

    ChatRenamedEvent chatRenamedEvent = new ChatRenamedEvent(UUID.randomUUID(), chat.getId(), oldName, chat.getName());

    eventPublisher.publish(KafkaTopics.CHAT_EVENTS,
        EventType.CHAT_RENAMED,
        String.valueOf(chat.getId()),
        chatRenamedEvent);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean isParticipant(Long chatId, Long userId) {

    Chat chat = loadChat(chatId);

    return chat.hasMember(userId);
  }

  private Chat loadChat(Long chatId) {
    return chatRepository
        .findByIdWithMembers(chatId)
        .orElseThrow(() -> new NotFoundException("Chat not found"));
  }

  private User loadUser(Long userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new NotFoundException("User not found"));
  }

  // =========================
  // MAPPING
  // =========================

  private ChatDto toDto(Chat chat) {
    return ChatDto.toDto(chat);
  }
}
