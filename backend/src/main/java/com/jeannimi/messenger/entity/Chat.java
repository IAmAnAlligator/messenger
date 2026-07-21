package com.jeannimi.messenger.entity;

import com.jeannimi.messenger.exception_handling.ChatError;
import com.jeannimi.messenger.exception_handling.ChatException;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.annotations.BatchSize;

@Slf4j
@Entity
@Table(name = "chats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class Chat {

  private static final int MAX_CHAT_NAME_LENGTH = 100;
  private static final String SEPARATOR = "_";

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "name")
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private ChatType type;

  @OneToMany(
      mappedBy = "chat",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  @BatchSize(size = 20)
  private Set<ChatMember> members = new HashSet<>();

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "last_message_at")
  private Instant lastMessageAt;

  @Column(name = "private_key", unique = true, updatable = false)
  private String privateKey;

  @Version private Long version;

  public static String buildPrivateKey(Long u1, Long u2) {
    Objects.requireNonNull(u1);
    Objects.requireNonNull(u2);

    long min = Math.min(u1, u2);
    long max = Math.max(u1, u2);

    return min + SEPARATOR + max;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;

    if (!(o instanceof Chat that)) return false;

    return id != null && id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

  public boolean hasMember(Long userId) {
    return members.stream().anyMatch(m -> m.getUser().getId().equals(userId));
  }

  public void updateLastMessageTime() {
    this.lastMessageAt = Instant.now().truncatedTo(ChronoUnit.MILLIS);
  }

  public void setName(String name) {
    if (name != null && name.isBlank()) {
      throw new ChatException(
          ChatError.CHAT_NAME_EMPTY,
          "Name must not be blank");
    }
    this.name = name != null ? name.trim() : null;
  }

  public static Chat createGroup(String name, User creator, List<User> users) {

    if (name == null || name.isBlank()) {
      throw new ChatException(
          ChatError.GROUP_NAME_BLANK,
          "Group name must not be blank");
    }

    Instant now = Instant.now();

    Chat chat = new Chat();
    chat.name = name.trim();
    chat.type = ChatType.GROUP;
    chat.createdAt = now;
    chat.lastMessageAt = now;

    chat.addMemberInternal(creator, ChatRole.ADMIN);

    Objects.requireNonNull(users);

    for (User user : users) {
      if (!user.getId().equals(creator.getId())) {
        chat.addMemberInternal(user, ChatRole.MEMBER);
      }
    }

    return chat;
  }

  public static Chat createPrivate(User userA, User userB) {

    if (userA.getId().equals(userB.getId())) {
      throw new ChatException(
          ChatError.CANNOT_CREATE_PRIVATE_WITH_YOURSELF,
          "Cannot create chat with yourself");
    }

    Instant now = Instant.now();

    Chat chat = new Chat();

    chat.name = userA.getUsername().getValue() + "_" + userB.getUsername().getValue();

    chat.type = ChatType.PRIVATE;
    chat.createdAt = now;
    chat.lastMessageAt = now;

    chat.privateKey = buildPrivateKey(userA.getId(), userB.getId());

    chat.addMemberInternal(userA, ChatRole.ADMIN);
    chat.addMemberInternal(userB, ChatRole.MEMBER);

    return chat;
  }

  public boolean isPrivate() {
    return type == ChatType.PRIVATE;
  }

  public void validate() {
    if (type == ChatType.PRIVATE) {
      if (members.size() != 2) {
        throw new ChatException(
            ChatError.PRIVATE_CHAT_MUST_HAVE_TWO_MEMBERS,
            "Private chat must have exactly 2 members");
      }
      if (privateKey == null) {
        throw new ChatException(
            ChatError.PRIVATE_CHAT_MUST_HAVE_PRIVATE_KEY,
            "Private chat must have privateKey");
      }
    }
  }

  public void addMember(User user, Long currentUserId) {

    requireGroup();

    ChatMember currentUser = getMember(currentUserId);

    requireMember(currentUser);

    requireAdmin(currentUser);

    if (hasMember(user.getId())) {
      throw new ChatException(
          ChatError.USER_ALREADY_IN_CHAT,
          "User already in chat");
    }

    addMemberInternal(user, ChatRole.MEMBER);
  }

  private void addMemberInternal(User user, ChatRole role) {

    if (hasMember(user.getId())) {
      throw new ChatException(
          ChatError.USER_ALREADY_IN_CHAT,
          "User already in chat");
    }

    ChatMember member = ChatMember.of(user, role);
    member.setChat(this);

    members.add(member);
  }

  public ChatMember getMember(Long userId) {
    return members.stream()
        .filter(m -> m.getUser().getId().equals(userId))
        .findFirst()
        .orElse(null);
  }

  public long countAdmins() {
    return members.stream().filter(m -> m.getRole() == ChatRole.ADMIN).count();
  }

  public void removeMember(Long targetUserId, Long currentUserId) {

    requireGroup();

    ChatMember currentUser = getMember(currentUserId);

    requireMember(currentUser);

    requireAdmin(currentUser);

    if (targetUserId.equals(currentUserId)) {
      throw new ChatException(
          ChatError.CANNOT_REMOVE_YOURSELF,
          "Cannot remove yourself");
    }

    ChatMember target = getMember(targetUserId);
    if (target == null) {
      throw new ChatException(
          ChatError.MEMBER_NOT_FOUND,
          "Member not found");
    }

    if (target.isAdmin() && countAdmins() <= 1) {
      throw new ChatException(
          ChatError.LAST_ADMIN_CANNOT_BE_REMOVED,
          "Cannot remove last admin");
    }

    removeMemberInternal(target);
  }

  public void leaveChat(Long currentUserId) {

    requireGroup();

    ChatMember currentUser = getMember(currentUserId);

    requireMember(currentUser);

    if (currentUser.isAdmin()) {
      throw new ChatException(
          ChatError.ADMIN_CANNOT_LEAVE,
          "Admin cannot leave chat");
    }

    removeMemberInternal(currentUser);
  }

  public void renameChat(Long currentUserId, String chatName) {

    requireGroup();

    ChatMember currentUser = getMember(currentUserId);

    requireMember(currentUser);

    requireAdmin(currentUser);

    renameChatInternal(chatName);
  }

  private void renameChatInternal(String chatName) {

    if (chatName == null || chatName.isBlank()) {
      throw new ChatException(
          ChatError.CHAT_NAME_EMPTY,
          "Chat name cannot be empty");
    }

    chatName = chatName.trim();

    if (chatName.length() > MAX_CHAT_NAME_LENGTH) {
      throw new ChatException(
          ChatError.CHAT_NAME_TOO_LONG,
          "Chat name is too long");
    }

    this.name = chatName;
  }

  private void removeMemberInternal(ChatMember member) {
    if (!members.remove(member)) {
      throw new ChatException(
          ChatError.MEMBER_NOT_IN_CHAT,
          "Member not in this chat");
    }
    member.setChat(null);
  }

  public void ensureCanDelete(Long currentUserId) {

    ChatMember currentUser = getMember(currentUserId);

    requireMember(currentUser);

    if (!this.isPrivate() && !currentUser.isAdmin()) {
      throw new ChatException(
          ChatError.ONLY_ADMIN_ALLOWED,
          "Only admins can do this");
    }
  }

  private void requireMember(ChatMember currentUser) {
    if (currentUser == null) {
      throw new ChatException(
          ChatError.NOT_CHAT_MEMBER,
          "Not a member of this chat");
    }
  }

  private void requireAdmin(ChatMember currentUser) {
    if (!currentUser.isAdmin()) {
      throw new ChatException(
          ChatError.ONLY_ADMIN_ALLOWED,
          "Only admins can do this");
    }
  }

  private void requireGroup() {
    if (isPrivate()) {
      throw new ChatException(
          ChatError.PRIVATE_CHAT_OPERATION_NOT_ALLOWED,
          "Operation is not allowed for private chats");
    }
  }
}
