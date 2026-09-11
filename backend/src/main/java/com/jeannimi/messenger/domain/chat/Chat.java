package com.jeannimi.messenger.domain.chat;

import com.jeannimi.messenger.common.exception_handling.ChatError;
import com.jeannimi.messenger.common.exception_handling.ChatException;
import com.jeannimi.messenger.domain.user.User;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;

@Getter
public final class Chat {

  private final Long id;

  private String name;
  private final ChatType type;
  private final Set<ChatMember> members;

  private final Instant createdAt;
  private Instant lastMessageAt;
  private final String privateKey;

  private Chat(
      Long id,
      String name,
      ChatType type,
      Set<ChatMember> members,
      Instant createdAt,
      Instant lastMessageAt,
      String privateKey) {

    this.id = id;
    this.name = Objects.requireNonNull(name, "name");
    this.type = Objects.requireNonNull(type, "type");
    this.members = new HashSet<>(Objects.requireNonNull(members, "members"));
    this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
    this.lastMessageAt = lastMessageAt;
    this.privateKey = privateKey;
  }

  public static Chat reconstitute(
      Long id,
      String name,
      ChatType type,
      Set<ChatMember> members,
      Instant createdAt,
      Instant lastMessageAt,
      String privateKey) {

    Chat chat =
        new Chat(
            Objects.requireNonNull(id, "id"),
            name,
            type,
            members,
            createdAt,
            lastMessageAt,
            privateKey);

    chat.validateState();

    return chat;
  }

  private void validateState() {

    switch (type) {
      case GROUP -> validateGroup();
      case PRIVATE -> validatePrivate();
    }
  }

  private void validateGroup() {

    if (members.isEmpty()) {
      throw new ChatException(
          ChatError.GROUP_MUST_HAVE_MEMBERS, "Group must have at least one member");
    }

    if (members.size() > ChatConstants.MAX_GROUP_MEMBERS) {
      throw new ChatException(
          ChatError.GROUP_MEMBER_LIMIT_EXCEEDED,
          "Group cannot contain more than " + ChatConstants.MAX_GROUP_MEMBERS + " members");
    }

    if (countAdmins() == 0) {
      throw new ChatException(
          ChatError.GROUP_MUST_HAVE_ADMIN, "Group must have at least one admin");
    }
  }

  public static Chat createGroup(String name, User creator, List<User> users) {

    Objects.requireNonNull(creator, "creator");
    Objects.requireNonNull(users, "users");

    Instant now = Instant.now();

    Chat chat =
        new Chat(
            null, validateAndNormalizeName(name), ChatType.GROUP, new HashSet<>(), now, null, null);

    chat.addMemberInternal(creator, ChatRole.ADMIN);

    for (User user : users) {
      Objects.requireNonNull(user, "user");

      if (!user.getId().equals(creator.getId())) {

        if (chat.members.size() >= ChatConstants.MAX_GROUP_MEMBERS) {
          throw new ChatException(
              ChatError.GROUP_MEMBER_LIMIT_EXCEEDED,
              "Group cannot contain more than " + ChatConstants.MAX_GROUP_MEMBERS + " members");
        }

        chat.addMemberInternal(user, ChatRole.MEMBER);
      }
    }

    if (chat.members.size() < ChatConstants.MIN_GROUP_MEMBERS) {
      throw new ChatException(
          ChatError.GROUP_MUST_HAVE_MINIMUM_MEMBERS,
          "Group must contain at least " + ChatConstants.MIN_GROUP_MEMBERS + " members");
    }

    chat.validateState();

    return chat;
  }

  public static Chat createPrivate(User userA, User userB) {

    Objects.requireNonNull(userA, "userA");
    Objects.requireNonNull(userB, "userB");

    if (userA.getId().equals(userB.getId())) {
      throw new ChatException(
          ChatError.CANNOT_CREATE_PRIVATE_WITH_YOURSELF, "Cannot create chat with yourself");
    }

    Instant now = Instant.now();

    String name = userA.getUsername().getValue() + "_" + userB.getUsername().getValue();

    String privateKey = buildPrivateKey(userA.getId(), userB.getId());

    Set<ChatMember> members = new HashSet<>();

    Chat chat = new Chat(null, name, ChatType.PRIVATE, members, now, null, privateKey);

    chat.addMemberInternal(userA, ChatRole.ADMIN);

    chat.addMemberInternal(userB, ChatRole.MEMBER);

    chat.validateState();

    return chat;
  }

  public static String buildPrivateKey(Long u1, Long u2) {

    Objects.requireNonNull(u1, "First user id");
    Objects.requireNonNull(u2, "Second user id");

    long min = Math.min(u1, u2);
    long max = Math.max(u1, u2);

    return min + ChatConstants.SEPARATOR + max;
  }

  public boolean hasMember(Long userId) {
    Objects.requireNonNull(userId, "userId");
    return members.stream().anyMatch(member -> member.getUserId().equals(userId));
  }

  public void updateLastMessageTime() {
    this.lastMessageAt = Instant.now().truncatedTo(ChronoUnit.MILLIS);
  }

  public boolean isPrivate() {
    return type == ChatType.PRIVATE;
  }

  public void addMember(User user, Long currentUserId) {

    Objects.requireNonNull(user, "user");
    Objects.requireNonNull(currentUserId, "currentUserId");

    requireGroup();

    ChatMember currentUser = getMember(currentUserId);

    requireMember(currentUser);
    requireAdmin(currentUser);

    if (hasMember(user.getId())) {
      throw new ChatException(ChatError.USER_ALREADY_IN_CHAT, "User already in chat");
    }

    if (members.size() >= ChatConstants.MAX_GROUP_MEMBERS) {
      throw new ChatException(
          ChatError.GROUP_MEMBER_LIMIT_EXCEEDED,
          "Group cannot contain more than " + ChatConstants.MAX_GROUP_MEMBERS + " members");
    }

    addMemberInternal(user, ChatRole.MEMBER);
  }

  public void removeMember(Long targetUserId, Long currentUserId) {

    Objects.requireNonNull(targetUserId, "targetUserId");
    Objects.requireNonNull(currentUserId, "currentUserId");

    requireGroup();

    ChatMember currentUser = getMember(currentUserId);

    requireMember(currentUser);
    requireAdmin(currentUser);

    if (targetUserId.equals(currentUserId)) {
      throw new ChatException(ChatError.CANNOT_REMOVE_YOURSELF, "Cannot remove yourself");
    }

    ChatMember target = getMember(targetUserId);

    if (target == null) {
      throw new ChatException(ChatError.MEMBER_NOT_FOUND, "Member not found");
    }

    if (target.isAdmin() && countAdmins() <= 1) {

      throw new ChatException(ChatError.LAST_ADMIN_CANNOT_BE_REMOVED, "Cannot remove last admin");
    }

    removeMemberInternal(target);
  }

  public void leaveChat(Long currentUserId) {

    Objects.requireNonNull(currentUserId, "currentUserId");

    requireGroup();

    ChatMember currentUser = getMember(currentUserId);

    requireMember(currentUser);

    if (currentUser.isAdmin()) {
      throw new ChatException(ChatError.ADMIN_CANNOT_LEAVE, "Admin cannot leave chat");
    }

    removeMemberInternal(currentUser);
  }

  public void renameChat(Long currentUserId, String chatName) {

    Objects.requireNonNull(currentUserId, "currentUserId");

    requireGroup();

    ChatMember currentUser = getMember(currentUserId);

    requireMember(currentUser);
    requireAdmin(currentUser);

    setName(chatName);
  }

  private static String validateAndNormalizeName(String name) {

    if (name == null || name.isBlank()) {
      throw new ChatException(ChatError.CHAT_NAME_EMPTY, "Chat name cannot be empty");
    }

    String normalizedName = name.trim();

    if (normalizedName.length() > ChatConstants.MAX_CHAT_NAME_LENGTH) {
      throw new ChatException(ChatError.CHAT_NAME_TOO_LONG, "Chat name is too long");
    }

    return normalizedName;
  }

  private void setName(String name) {
    this.name = validateAndNormalizeName(name);
  }

  public void ensureCanDelete(Long currentUserId) {

    Objects.requireNonNull(currentUserId, "currentUserId");

    ChatMember currentUser = getMember(currentUserId);

    requireMember(currentUser);

    if (!isPrivate() && !currentUser.isAdmin()) {

      throw new ChatException(ChatError.ONLY_ADMIN_ALLOWED, "Only admins can do this");
    }
  }

  private void addMemberInternal(User user, ChatRole role) {

    if (hasMember(user.getId())) {
      throw new ChatException(ChatError.USER_ALREADY_IN_CHAT, "User already in chat");
    }

    ChatMember member = ChatMember.create(user.getId(), role);

    members.add(member);
  }

  private ChatMember getMember(Long userId) {

    return members.stream()
        .filter(member -> member.getUserId().equals(userId))
        .findFirst()
        .orElse(null);
  }

  private long countAdmins() {

    return members.stream().filter(member -> member.getRole() == ChatRole.ADMIN).count();
  }

  private void removeMemberInternal(ChatMember member) {

    if (!members.remove(member)) {
      throw new ChatException(ChatError.MEMBER_NOT_IN_CHAT, "Member not in this chat");
    }
  }

  private void validatePrivate() {

    if (members.size() != 2) {
      throw new ChatException(
          ChatError.PRIVATE_CHAT_MUST_HAVE_TWO_MEMBERS, "Private chat must have exactly 2 members");
    }

    if (privateKey == null || privateKey.isBlank()) {
      throw new ChatException(
          ChatError.PRIVATE_CHAT_MUST_HAVE_PRIVATE_KEY, "Private chat must have privateKey");
    }
  }

  private void requireMember(ChatMember currentUser) {

    if (currentUser == null) {
      throw new ChatException(ChatError.NOT_CHAT_MEMBER, "Not a member of this chat");
    }
  }

  private void requireAdmin(ChatMember currentUser) {

    if (!currentUser.isAdmin()) {
      throw new ChatException(ChatError.ONLY_ADMIN_ALLOWED, "Only admins can do this");
    }
  }

  private void requireGroup() {

    if (isPrivate()) {
      throw new ChatException(
          ChatError.PRIVATE_CHAT_OPERATION_NOT_ALLOWED,
          "Operation is not allowed for private chats");
    }
  }

  public Set<ChatMember> getMembers() {
    return Collections.unmodifiableSet(members);
  }

  @Override
  public boolean equals(Object o) {

    if (this == o) {
      return true;
    }

    if (!(o instanceof Chat that)) {
      return false;
    }

    return id != null && id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
