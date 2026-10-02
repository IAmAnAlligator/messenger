package com.jeannimi.messenger.application.chat.service;

import com.jeannimi.messenger.application.chat.ChatApplicationConstants;
import com.jeannimi.messenger.application.chat.command.ChatCreateCommand;
import com.jeannimi.messenger.application.chat.command.RenameChatCommand;
import com.jeannimi.messenger.application.chat.dto.ChatMemberResult;
import com.jeannimi.messenger.application.chat.dto.ChatResult;
import com.jeannimi.messenger.application.common.pagination.Cursor;
import com.jeannimi.messenger.application.common.pagination.CursorPageQuery;
import com.jeannimi.messenger.application.common.pagination.CursorPageResult;
import com.jeannimi.messenger.application.event.ChatCreatedEvent;
import com.jeannimi.messenger.application.event.ChatDeletedEvent;
import com.jeannimi.messenger.application.event.ChatMemberAddedEvent;
import com.jeannimi.messenger.application.event.ChatMemberLeftEvent;
import com.jeannimi.messenger.application.event.ChatMemberRemovedEvent;
import com.jeannimi.messenger.application.event.ChatRenamedEvent;
import com.jeannimi.messenger.application.event.EventType;
import com.jeannimi.messenger.application.exception.BadRequestException;
import com.jeannimi.messenger.application.exception.ConflictException;
import com.jeannimi.messenger.application.exception.ForbiddenException;
import com.jeannimi.messenger.application.exception.NotFoundException;
import com.jeannimi.messenger.application.message.service.MessageService;
import com.jeannimi.messenger.application.port.out.ChatMemberRepositoryPort;
import com.jeannimi.messenger.application.port.out.ChatRepositoryPort;
import com.jeannimi.messenger.application.port.out.EventPublisherPort;
import com.jeannimi.messenger.application.port.out.IdGenerator;
import com.jeannimi.messenger.application.port.out.UserRepositoryPort;
import com.jeannimi.messenger.application.user.dto.UserResult;
import com.jeannimi.messenger.domain.chat.Chat;
import com.jeannimi.messenger.domain.chat.ChatId;
import com.jeannimi.messenger.domain.chat.ChatMember;
import com.jeannimi.messenger.domain.chat.ChatMemberId;
import com.jeannimi.messenger.domain.chat.ChatRole;
import com.jeannimi.messenger.domain.outbox.AggregateId;
import com.jeannimi.messenger.domain.user.User;
import com.jeannimi.messenger.domain.user.UserId;
import java.time.Instant;
import java.util.ArrayList;
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
  private final IdGenerator idGenerator;

  // =========================
  // CREATE CHAT
  // =========================

  @Override
  @Transactional
  public ChatResult createChat(ChatCreateCommand command, UserId currentUserId) {

    User creator = loadUser(currentUserId);

    return switch (command.type()) {
      case PRIVATE -> createPrivateChat(command, creator);
      case GROUP -> createGroupChat(command, creator);
    };
  }

  private ChatResult createPrivateChat(ChatCreateCommand command, User creator) {

    if (command.memberIds() == null || command.memberIds().size() != 1) {
      throw new BadRequestException("Private chat must have exactly one member");
    }

    UserId otherUserId = new UserId(command.memberIds().get(0));

    User otherUser = loadUser(otherUserId);

    String key = Chat.buildPrivateKey(creator.getId(), otherUser.getId());

    if (chatRepository.findByPrivateKey(key).isPresent()) {
      throw new ConflictException("Private chat already exists");
    }

    ChatId chatId = new ChatId(idGenerator.generate());

    ChatMemberId creatorMemberId = new ChatMemberId(idGenerator.generate());

    ChatMemberId otherMemberId = new ChatMemberId(idGenerator.generate());

    ChatMember creatorMember = ChatMember.create(creatorMemberId, creator, ChatRole.ADMIN);

    ChatMember otherMember = ChatMember.create(otherMemberId, otherUser, ChatRole.MEMBER);

    Chat chat = Chat.createPrivate(chatId, creatorMember, otherMember);

    return savePrivateChat(chat);
  }

  private ChatResult savePrivateChat(Chat chat) {

    try {
      Chat saved = chatRepository.save(chat);

      ChatResult result = toResult(saved);

      publishChatCreated(saved);

      return result;

    } catch (DataIntegrityViolationException e) {
      throw new ConflictException("Private chat already exists");
    }
  }

  private ChatResult createGroupChat(ChatCreateCommand command, User creator) {

    List<UserId> userIds = new ArrayList<>();

    for (int i = 0; i < command.memberIds().size(); i++) {
      userIds.add(new UserId(command.memberIds().get(i)));
    }

    Set<UserId> uniqueIds = new HashSet<>(userIds);

    uniqueIds.remove(creator.getId());

    List<User> users = userRepository.findAllById(uniqueIds);

    if (users.size() != uniqueIds.size()) {
      throw new NotFoundException("One or more users not found");
    }

    ChatId chatId = new ChatId(idGenerator.generate());

    List<ChatMember> members = new ArrayList<>();

    ChatMember creatorMember =
        ChatMember.create(new ChatMemberId(idGenerator.generate()), creator, ChatRole.ADMIN);

    members.add(creatorMember);

    for (User user : users) {

      ChatMember member =
          ChatMember.create(new ChatMemberId(idGenerator.generate()), user, ChatRole.MEMBER);

      members.add(member);
    }

    Chat chat = Chat.createGroup(chatId, command.name(), members);

    Chat saved = chatRepository.save(chat);

    ChatResult result = toResult(saved);

    publishChatCreated(saved);

    return result;
  }

  private void publishChatCreated(Chat chat) {

    ChatCreatedEvent event =
        new ChatCreatedEvent(
            chat.getId(),
            chat.getName(),
            chat.getType(),
            chat.getMembers().stream().map(ChatMember::getUserId).toList());

    eventPublisher.publish(EventType.CHAT_CREATED, new AggregateId(chat.getId().value()), event);
  }

  // =========================
  // GET USER CHATS
  // =========================

  @Override
  @Transactional(readOnly = true)
  public CursorPageResult<ChatResult, ChatId> getUserChats(
      UserId userId, CursorPageQuery<ChatId> query) {

    int pageSize = Math.min(query.limit(), ChatApplicationConstants.MAX_CHAT_PAGE_SIZE);

    int fetchSize = pageSize + 1;

    List<ChatId> ids =
        query.cursorTime() == null
            ? chatMemberRepository.findFirstPageIds(userId, fetchSize)
            : chatMemberRepository.findNextPageIds(
                userId, query.cursorTime(), query.cursorId(), fetchSize);

    if (ids.isEmpty()) {
      return emptyPage();
    }

    boolean hasMore = ids.size() > pageSize;

    ids = takePage(ids, pageSize);

    List<Chat> chats = chatRepository.findByIdsWithMembers(ids);

    List<Chat> orderedChats = restoreOrder(ids, chats);

    Cursor<ChatId> nextCursor = createNextCursor(orderedChats, hasMore);

    return new CursorPageResult<>(
        orderedChats.stream().map(this::toResult).toList(), nextCursor, hasMore);
  }

  private List<ChatId> takePage(List<ChatId> ids, int pageSize) {

    if (ids.size() <= pageSize) {
      return ids;
    }

    return ids.subList(0, pageSize);
  }

  private CursorPageResult<ChatResult, ChatId> emptyPage() {
    return new CursorPageResult<>(List.of(), null, false);
  }

  private List<Chat> restoreOrder(List<ChatId> ids, List<Chat> chats) {

    Map<ChatId, Chat> chatMap =
        chats.stream().collect(Collectors.toMap(Chat::getId, Function.identity()));

    return ids.stream().map(chatMap::get).filter(Objects::nonNull).toList();
  }

  private Cursor<ChatId> createNextCursor(List<Chat> chats, boolean hasMore) {

    if (!hasMore || chats.isEmpty()) {
      return null;
    }

    Chat lastChat = chats.get(chats.size() - 1);

    Instant cursorTime =
        lastChat.getLastMessageAt() != null ? lastChat.getLastMessageAt() : lastChat.getCreatedAt();

    return new Cursor<>(cursorTime, lastChat.getId());
  }

  // =========================
  // GET CHAT
  // =========================

  @Override
  @Transactional(readOnly = true)
  public ChatResult getChat(ChatId chatId, UserId userId) {

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
  public void addMember(ChatId chatId, UserId userId, UserId currentUserId) {

    Chat chat = loadChat(chatId);

    User user = loadUser(userId);

    ChatMember member =
        ChatMember.create(new ChatMemberId(idGenerator.generate()), user, ChatRole.MEMBER);

    chat.addMember(member, currentUserId);

    chatRepository.save(chat);

    ChatMemberAddedEvent chatMemberAddedEvent =
        new ChatMemberAddedEvent(chat.getId(), user.getId(), user.getUsername().getValue());

    eventPublisher.publish(
        EventType.CHAT_MEMBER_ADDED, new AggregateId(chat.getId().value()), chatMemberAddedEvent);
  }

  // =========================
  // REMOVE MEMBER
  // =========================

  @Override
  @Transactional
  public void removeMember(ChatId chatId, UserId userId, UserId currentUserId) {

    Chat chat = loadChat(chatId);

    chat.removeMember(userId, currentUserId);

    chatRepository.save(chat);

    ChatMemberRemovedEvent chatMemberRemovedEvent =
        new ChatMemberRemovedEvent(chat.getId(), userId);

    eventPublisher.publish(
        EventType.CHAT_MEMBER_REMOVED,
        new AggregateId(chat.getId().value()),
        chatMemberRemovedEvent);
  }

  @Override
  @Transactional
  public void deleteChat(ChatId chatId, UserId currentUserId) {

    Chat chat = loadChat(chatId);

    chat.ensureCanDelete(currentUserId);

    List<UserId> recipientUserIds = chat.getMembers().stream().map(ChatMember::getUserId).toList();

    messageService.deleteAllByChat(chatId);

    chatRepository.delete(chat);

    ChatDeletedEvent chatDeletedEvent = new ChatDeletedEvent(chatId, recipientUserIds);

    eventPublisher.publish(
        EventType.CHAT_DELETED, new AggregateId(chat.getId().value()), chatDeletedEvent);
  }

  @Override
  @Transactional
  public void leaveChat(ChatId chatId, UserId currentUserId) {

    Chat chat = loadChat(chatId);

    chat.leaveChat(currentUserId);

    chatRepository.save(chat);

    ChatMemberLeftEvent chatMemberLeftEvent = new ChatMemberLeftEvent(chat.getId(), currentUserId);

    eventPublisher.publish(
        EventType.CHAT_MEMBER_LEFT, new AggregateId(chat.getId().value()), chatMemberLeftEvent);
  }

  @Override
  @Transactional(readOnly = true)
  public List<ChatMemberResult> getMembers(ChatId chatId, UserId currentUserId) {

    Chat chat = loadChat(chatId);

    if (!chat.hasMember(currentUserId)) {
      throw new ForbiddenException("Access denied");
    }

    return chat.getMembers().stream().map(this::toMemberResult).toList();
  }

  @Transactional
  @Override
  public void renameChat(ChatId chatId, RenameChatCommand command, UserId currentUserId) {

    Chat chat = loadChat(chatId);

    String oldName = chat.getName();

    List<UserId> recipientUserIds = chat.getMembers().stream().map(ChatMember::getUserId).toList();

    chat.renameChat(currentUserId, command.name());

    chatRepository.save(chat);

    String newName = chat.getName();

    ChatRenamedEvent event = new ChatRenamedEvent(chat.getId(), oldName, newName, recipientUserIds);

    eventPublisher.publish(EventType.CHAT_RENAMED, new AggregateId(chat.getId().value()), event);
  }

  @Override
  @Transactional(readOnly = true)
  public boolean isParticipant(ChatId chatId, UserId userId) {

    return chatMemberRepository.existsByChatIdAndUserId(chatId, userId);
  }

  private Chat loadChat(ChatId chatId) {
    return chatRepository
        .findByIdWithMembers(chatId)
        .orElseThrow(() -> new NotFoundException("Chat not found"));
  }

  private User loadUser(UserId userId) {
    return userRepository
        .findById(userId)
        .orElseThrow(() -> new NotFoundException("User not found"));
  }

  // =========================
  // MAPPING
  // =========================

  private ChatResult toResult(Chat chat) {

    List<ChatMemberResult> memberResults =
        chat.getMembers().stream().map(this::toMemberResult).toList();

    return new ChatResult(
        chat.getId(),
        chat.getName(),
        chat.getType().name(),
        memberResults,
        chat.getCreatedAt(),
        chat.getLastMessageAt());
  }

  private ChatMemberResult toMemberResult(ChatMember member) {

    User user = member.getUser();

    UserResult userResult =
        new UserResult(
            user.getId(),
            user.getHandle().getValue(),
            user.getUsername().getValue(),
            user.getRole());

    return new ChatMemberResult(
        userResult, member.getRole(), member.getJoinedAt(), member.getLastReadMessageId());
  }
}
