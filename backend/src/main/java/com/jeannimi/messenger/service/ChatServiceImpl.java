package com.jeannimi.messenger.service;

import static com.jeannimi.messenger.entity.Chat.buildPrivateKey;

import com.jeannimi.messenger.dto.ChatCreateRequest;
import com.jeannimi.messenger.dto.ChatDto;
import com.jeannimi.messenger.dto.ChatMemberDto;
import com.jeannimi.messenger.dto.UserDto;
import com.jeannimi.messenger.entity.Chat;
import com.jeannimi.messenger.entity.User;
import com.jeannimi.messenger.exception_handling.BadRequestException;
import com.jeannimi.messenger.exception_handling.ConflictException;
import com.jeannimi.messenger.exception_handling.ForbiddenException;
import com.jeannimi.messenger.exception_handling.NotFoundException;
import com.jeannimi.messenger.kafka.ChatEventProducer;
import com.jeannimi.messenger.mapper.ChatMapper;
import com.jeannimi.messenger.repository.ChatMemberRepository;
import com.jeannimi.messenger.repository.ChatRepository;
import com.jeannimi.messenger.repository.UserRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ChatServiceImpl implements ChatService {

  private final ChatRepository chatRepository;
  private final UserRepository userRepository;
  private final ChatMemberRepository chatMemberRepository;
  private final MessageService messageService;
  private final ChatEventProducer chatEventProducer;

  // =========================
  // CREATE CHAT
  // =========================

  @Override
  public ChatDto createChat(ChatCreateRequest request, Long currentUserId) {

    User creator =
        userRepository
            .findById(currentUserId)
            .orElseThrow(() -> new NotFoundException("User not found"));

    return switch (request.getType()) {
      case PRIVATE -> createPrivateChat(request, creator);
      case GROUP -> createGroupChat(request, creator);
      default -> throw new BadRequestException("Unsupported chat type");
    };
  }

  private ChatDto createPrivateChat(ChatCreateRequest request, User creator) {

    if (request.getMemberIds() == null || request.getMemberIds().size() != 1) {
      throw new BadRequestException("Private chat must have exactly one member");
    }

    Long otherUserId = request.getMemberIds().get(0);

    String key = buildPrivateKey(creator.getId(), otherUserId);

    Optional<Chat> existing = chatRepository.findByPrivateKey(key);

    if (existing.isPresent()) {
      throw new ConflictException("Private chat already exists");
    }

    User otherUser =
        userRepository
            .findById(otherUserId)
            .orElseThrow(() -> new NotFoundException("User not found"));

    Chat chat = Chat.createPrivate(creator, otherUser);

    chat.validate();

    try {
      return toDto(chatRepository.save(chat));
    } catch (DataIntegrityViolationException e) {
      return toDto(
          chatRepository
              .findByPrivateKey(key)
              .orElseThrow(() -> new ConflictException("Private chat already exists")));
    }
  }

  private ChatDto createGroupChat(ChatCreateRequest request, User creator) {

    if (request.getName() == null || request.getName().isBlank()) {
      throw new BadRequestException("Group name is required");
    }

    Set<Long> uniqueIds =
        request.getMemberIds() == null ? new HashSet<>() : new HashSet<>(request.getMemberIds());

    uniqueIds.remove(creator.getId());

    List<User> users = userRepository.findAllById(uniqueIds);

    if (uniqueIds.isEmpty()) {
      throw new BadRequestException("Group must have at least one member");
    }

    if (users.size() != uniqueIds.size()) {
      throw new NotFoundException("One or more users not found");
    }

    Chat chat = Chat.createGroup(request.getName(), creator, users);

    return toDto(chatRepository.save(chat));
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

    Chat chat =
        chatRepository.findById(chatId).orElseThrow(() -> new NotFoundException("Chat not found"));

    if (!chat.hasMember(userId)) {
      throw new ForbiddenException("Access denied");
    }

    return toDto(chat);
  }

  // =========================
  // ADD MEMBER
  // =========================

  @Override
  public void addMember(Long chatId, Long userId, Long currentUserId) {

    Chat chat =
        chatRepository
            .findByIdWithMembers(chatId)
            .orElseThrow(() -> new NotFoundException("Chat not found"));

    User user =
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

    try {
      chat.addMember(user, currentUserId);
      chatRepository.save(chat);
    } catch (DataIntegrityViolationException e) {
      throw new ConflictException("User already in chat");
    }
  }

  // =========================
  // REMOVE MEMBER
  // =========================

  @Override
  public void removeMember(Long chatId, Long userId, Long currentUserId) {

    Chat chat =
        chatRepository
            .findByIdWithMembers(chatId)
            .orElseThrow(() -> new NotFoundException("Chat not found"));

    chat.removeMember(userId, currentUserId);

    chatRepository.save(chat);
  }

  @Override
  @Transactional
  public void deleteChat(Long chatId, Long currentUserId) {

    Chat chat =
        chatRepository
            .findByIdWithMembers(chatId)
            .orElseThrow(() -> new NotFoundException("Chat not found"));

    chat.ensureCanDelete(currentUserId);

    messageService.deleteAllByChat(chatId);

    chatRepository.delete(chat);

    chatEventProducer.publishChatDeleted(chatId);
  }

  @Transactional(readOnly = true)
  public List<ChatMemberDto> getMembers(Long chatId, Long currentUserId) {

    Chat chat =
        chatRepository
            .findByIdWithMembers(chatId)
            .orElseThrow(() -> new NotFoundException("Chat not found"));

    boolean hasAccess =
        chat.getMembers().stream()
            .anyMatch(member -> member.getUser().getId().equals(currentUserId));

    if (!hasAccess) {

      throw new ForbiddenException("Access denied");
    }

    return chat.getMembers().stream()
        .map(
            member ->
                ChatMemberDto.builder()
                    .user(
                        UserDto.builder()
                            .id(member.getUser().getId())
                            .username(member.getUser().getUsername().getValue())
                            .role(member.getUser().getRole())
                            .build())
                    .chatRole(member.getRole())
                    .joinedAt(member.getJoinedAt())
                    .build())
        .toList();
  }

  // =========================
  // MAPPING
  // =========================

  private ChatDto toDto(Chat chat) {
    return ChatMapper.toDto(chat);
  }

  public boolean isParticipant(Long chatId, Long userId) {

    Chat chat = chatRepository.findById(chatId).orElseThrow();

    return chat.hasMember(userId);
  }
}
