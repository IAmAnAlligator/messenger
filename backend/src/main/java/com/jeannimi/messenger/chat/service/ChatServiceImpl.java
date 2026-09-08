package com.jeannimi.messenger.chat.service;

import static com.jeannimi.messenger.domain.chat.Chat.buildPrivateKey;
import static com.jeannimi.messenger.domain.chat.ChatType.GROUP;
import static com.jeannimi.messenger.domain.chat.ChatType.PRIVATE;

import com.jeannimi.messenger.application.chat.command.ChatCreateCommand;
import com.jeannimi.messenger.application.chat.command.RenameChatCommand;
import com.jeannimi.messenger.application.chat.dto.ChatMemberResult;
import com.jeannimi.messenger.application.chat.dto.ChatResult;
import com.jeannimi.messenger.application.event.ChatCreatedEvent;
import com.jeannimi.messenger.application.event.ChatDeletedEvent;
import com.jeannimi.messenger.application.event.ChatMemberAddedEvent;
import com.jeannimi.messenger.application.event.ChatMemberLeftEvent;
import com.jeannimi.messenger.application.event.ChatMemberRemovedEvent;
import com.jeannimi.messenger.application.event.ChatRenamedEvent;
import com.jeannimi.messenger.application.event.EventType;
import com.jeannimi.messenger.application.port.out.ChatMemberRepositoryPort;
import com.jeannimi.messenger.application.port.out.ChatRepositoryPort;
import com.jeannimi.messenger.application.port.out.EventPublisherPort;
import com.jeannimi.messenger.application.port.out.UserRepositoryPort;
import com.jeannimi.messenger.application.user.dto.UserResult;
import com.jeannimi.messenger.chat.ChatConstants;
import com.jeannimi.messenger.common.exception_handling.BadRequestException;
import com.jeannimi.messenger.common.exception_handling.ConflictException;
import com.jeannimi.messenger.common.exception_handling.ForbiddenException;
import com.jeannimi.messenger.common.exception_handling.NotFoundException;
import com.jeannimi.messenger.common.pagination.CursorDto;
import com.jeannimi.messenger.common.pagination.CursorPageRequest;
import com.jeannimi.messenger.common.pagination.CursorPageResponse;
import com.jeannimi.messenger.domain.chat.Chat;
import com.jeannimi.messenger.domain.chat.ChatMember;
import com.jeannimi.messenger.domain.chat.ChatType;
import com.jeannimi.messenger.domain.user.User;
import com.jeannimi.messenger.message.service.MessageService;
import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

  private final ChatRepositoryPort chatRepository;
  private final UserRepositoryPort userRepository;
  private final ChatMemberRepositoryPort chatMemberRepository;
  private final MessageService messageService;
  private final EventPublisherPort eventPublisher;

  // =========================
  // CREATE CHAT
  // =========================

  @Override
  @Transactional
  public ChatResult createChat(ChatCreateCommand command, Long currentUserId) {

    User creator = loadUser(currentUserId);

    return switch (command.type()) {
      case PRIVATE -> createPrivateChat(command, creator);
      case GROUP -> createGroupChat(command, creator);
      default -> throw new BadRequestException("Unsupported chat type");
    };
  }

  private ChatResult createPrivateChat(ChatCreateCommand command, User creator) {

    if (command.memberIds() == null || command.memberIds().size() != 1) {
      throw new BadRequestException("Private chat must have exactly one member");
    }

    Long otherUserId = command.memberIds().get(0);

    String key = buildPrivateKey(creator.getId(), otherUserId);

    if (chatRepository.findByPrivateKey(key).isPresent()) {
      throw new ConflictException("Private chat already exists");
    }

    User otherUser = loadUser(otherUserId);

    Chat chat = Chat.createPrivate(creator, otherUser);

    return savePrivateChat(key, chat);
  }

  private ChatResult savePrivateChat(String key, Chat chat) {

    try {
      Chat saved = chatRepository.save(chat);

      ChatResult result = toResult(saved);

      ChatCreatedEvent chatCreatedEvent =
          new ChatCreatedEvent(
              result.id(),
              result.name(),
              ChatType.valueOf(result.type()),
              result.members().stream().map(member -> member.user().id()).toList());

      eventPublisher.publish(
          EventType.CHAT_CREATED, String.valueOf(saved.getId()), chatCreatedEvent);

      return toResult(saved);

    } catch (DataIntegrityViolationException e) {
      return toResult(
          chatRepository
              .findByPrivateKey(key)
              .orElseThrow(() -> new ConflictException("Private chat already exists")));
    }
  }

  private ChatResult createGroupChat(ChatCreateCommand command, User creator) {

    Set<Long> uniqueIds =
        command.memberIds() == null ? new HashSet<>() : new HashSet<>(command.memberIds());

    uniqueIds.remove(creator.getId());

    List<User> users = userRepository.findAllById(uniqueIds);

    if (users.size() != uniqueIds.size()) {
      throw new NotFoundException("One or more users not found");
    }

    Chat chat = Chat.createGroup(command.name(), creator, users);

    Chat saved = chatRepository.save(chat);

    ChatResult result = toResult(saved);

    ChatCreatedEvent chatCreatedEvent =
        new ChatCreatedEvent(
            result.id(),
            result.name(),
            ChatType.valueOf(result.type()),
            result.members().stream().map(member -> member.user().id()).toList());

    eventPublisher.publish(EventType.CHAT_CREATED, String.valueOf(saved.getId()), chatCreatedEvent);

    return toResult(saved);
  }

  // =========================
  // GET USER CHATS
  // =========================

  @Override
  @Transactional(readOnly = true)
  public CursorPageResponse<ChatResult, CursorDto> getUserChats(
      Long userId, CursorPageRequest request) {

    int pageSize = Math.min(request.limit(), ChatConstants.MAX_CHAT_PAGE_SIZE);

    int fetchSize = pageSize + 1;

    List<Long> ids =
        request.cursorTime() == null
            ? chatMemberRepository.findFirstPageIds(userId, fetchSize)
            : chatMemberRepository.findNextPageIds(
                userId, request.cursorTime(), request.cursorId(), fetchSize);

    if (ids.isEmpty()) {
      return emptyPage();
    }

    boolean hasMore = ids.size() > pageSize;

    ids = takePage(ids, pageSize);

    List<Chat> chats = chatRepository.findByIdsWithMembers(ids);

    List<Chat> orderedChats = restoreOrder(ids, chats);

    CursorDto nextCursor = createNextCursor(orderedChats, hasMore);

    return new CursorPageResponse<>(
        orderedChats.stream().map(this::toResult).toList(), nextCursor, hasMore);
  }

  private List<Long> takePage(List<Long> ids, int pageSize) {
    if (ids.size() <= pageSize) {
      return ids;
    }
    return ids.subList(0, pageSize);
  }

  private CursorPageResponse<ChatResult, CursorDto> emptyPage() {
    return new CursorPageResponse<>(List.of(), null, false);
  }

  private List<Chat> restoreOrder(List<Long> ids, List<Chat> chats) {

    Map<Long, Chat> chatMap =
        chats.stream().collect(Collectors.toMap(Chat::getId, Function.identity()));

    return ids.stream().map(chatMap::get).filter(Objects::nonNull).toList();
  }

  private CursorDto createNextCursor(List<Chat> chats, boolean hasMore) {

    if (!hasMore || chats.isEmpty()) {
      return null;
    }

    Chat lastChat = chats.get(chats.size() - 1);

    Instant cursorTime =
        lastChat.getLastMessageAt() != null ? lastChat.getLastMessageAt() : lastChat.getCreatedAt();

    return new CursorDto(cursorTime, lastChat.getId());
  }

  // =========================
  // GET CHAT
  // =========================

  @Override
  @Transactional(readOnly = true)
  public ChatResult getChat(Long chatId, Long userId) {

    Chat chat = loadChat(chatId);

    if (!chat.hasMember(userId)) {
      throw new ForbiddenException("Access denied");
    }

    return toResult(chat);
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

    chatRepository.save(chat);

    ChatMemberAddedEvent chatMemberAddedEvent =
        new ChatMemberAddedEvent(chat.getId(), user.getId(), user.getUsername().getValue());

    eventPublisher.publish(
        EventType.CHAT_MEMBER_ADDED, String.valueOf(chat.getId()), chatMemberAddedEvent);
  }

  // =========================
  // REMOVE MEMBER
  // =========================

  @Override
  @Transactional
  public void removeMember(Long chatId, Long userId, Long currentUserId) {

    Chat chat = loadChat(chatId);

    User user = loadUser(userId);

    chat.removeMember(userId, currentUserId);

    chatRepository.save(chat);

    ChatMemberRemovedEvent chatMemberRemovedEvent =
        new ChatMemberRemovedEvent(chat.getId(), user.getId());

    eventPublisher.publish(
        EventType.CHAT_MEMBER_REMOVED, String.valueOf(chat.getId()), chatMemberRemovedEvent);
  }

  @Override
  @Transactional
  public void deleteChat(Long chatId, Long currentUserId) {

    Chat chat = loadChat(chatId);

    chat.ensureCanDelete(currentUserId);

    Long deletedChatId = chat.getId();

    messageService.deleteAllByChat(chatId);

    ChatDeletedEvent chatDeletedEvent = new ChatDeletedEvent(chatId);

    eventPublisher.publish(EventType.CHAT_DELETED, String.valueOf(deletedChatId), chatDeletedEvent);

    chatRepository.delete(chat);
  }

  @Override
  @Transactional
  public void leaveChat(Long chatId, Long currentUserId) {

    Chat chat = loadChat(chatId);

    User user = loadUser(currentUserId);

    chat.leaveChat(currentUserId);

    chatRepository.save(chat);

    ChatMemberLeftEvent chatMemberLeftEvent = new ChatMemberLeftEvent(chat.getId(), user.getId());

    eventPublisher.publish(
        EventType.CHAT_MEMBER_LEFT, String.valueOf(chat.getId()), chatMemberLeftEvent);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ChatMemberResult> getMembers(Long chatId, Long currentUserId) {

    Chat chat = loadChat(chatId);

    if (!chat.hasMember(currentUserId)) {
      throw new ForbiddenException("Access denied");
    }

    List<ChatMember> members =
        chat.getMembers() == null ? List.of() : chat.getMembers().stream().toList();

    Map<Long, User> users =
        userRepository
            .findAllById(members.stream().map(ChatMember::getUserId).collect(Collectors.toSet()))
            .stream()
            .collect(Collectors.toMap(User::getId, Function.identity()));

    return members.stream().map(member -> toMemberResult(member, users)).toList();
  }

  @Transactional
  @Override
  public void renameChat(Long chatId, RenameChatCommand command, Long currentUserId) {

    Chat chat = loadChat(chatId);

    String oldName = chat.getName();

    chat.renameChat(currentUserId, command.name());

    chatRepository.save(chat);

    String newName = chat.getName();

    ChatRenamedEvent event = new ChatRenamedEvent(chat.getId(), oldName, newName);

    eventPublisher.publish(EventType.CHAT_RENAMED, String.valueOf(chat.getId()), event);
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

  private ChatResult toResult(Chat chat) {

    List<ChatMember> members =
        chat.getMembers() == null ? List.of() : chat.getMembers().stream().toList();

    Map<Long, User> users =
        userRepository
            .findAllById(members.stream().map(ChatMember::getUserId).collect(Collectors.toSet()))
            .stream()
            .collect(Collectors.toMap(User::getId, Function.identity()));

    List<ChatMemberResult> memberResults =
        members.stream().map(member -> toMemberResult(member, users)).toList();

    return new ChatResult(
        chat.getId(),
        chat.getName(),
        chat.getType().name(),
        memberResults,
        chat.getCreatedAt(),
        chat.getLastMessageAt());
  }

  private ChatMemberResult toMemberResult(ChatMember member, Map<Long, User> users) {

    User user = users.get(member.getUserId());

    if (user == null) {
      throw new NotFoundException("User not found: " + member.getUserId());
    }

    UserResult userResult =
        new UserResult(user.getId(), user.getUsername().getValue(), user.getRole());

    return new ChatMemberResult(
        userResult, member.getRole(), member.getJoinedAt(), member.getLastReadMessageId());
  }
}
