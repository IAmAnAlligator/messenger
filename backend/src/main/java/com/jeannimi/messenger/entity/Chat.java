package com.jeannimi.messenger.entity;

import com.jeannimi.messenger.exception_handling.BadRequestException;
import com.jeannimi.messenger.exception_handling.ConflictException;
import com.jeannimi.messenger.exception_handling.ForbiddenException;
import com.jeannimi.messenger.exception_handling.NotFoundException;
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
import java.util.Collections;
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
@Entity // JPA-сущность → таблица chats
@Table(name = "chats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED) // нужен JPA
@AllArgsConstructor
public class Chat {

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  // Первичный ключ (auto-increment)
  private Long id;

  @Column(name = "name")
  // Название чата (может быть null для PRIVATE чатов)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  // Тип чата:
  // PRIVATE / GROUP / CHANNEL
  // STRING → безопасно при изменении enum
  private ChatType type;

  @OneToMany(
      mappedBy = "chat",
      cascade = CascadeType.ALL, // все операции (persist/remove) каскадятся
      orphanRemoval = true, // удалённый из коллекции участник удаляется из БД
      fetch = FetchType.LAZY // не грузим участников сразу
      )
  @BatchSize(size = 20)
  // Участники чата
  // - LAZY → не загружаем всегда
  // - BatchSize → уменьшает N+1 (Hibernate подтягивает пачками)
  // - Aggregate boundary: Chat владеет ChatMember
  private Set<ChatMember> members = new HashSet<>();

  @Column(name = "created_at", nullable = false)
  // Дата создания чата
  private Instant createdAt;

  @Column(name = "last_message_at")
  // Время последнего сообщения (для сортировки чатов в inbox)
  private Instant lastMessageAt;

  @Column(name = "private_key", unique = true, updatable = false)
  private String privateKey;

  @Version
  // Оптимистическая блокировка:
  // защищает от race condition (например, два пользователя добавляют участников одновременно)
  private Long version;

  //  @PrePersist
  //  @PreUpdate
  //  private void validate() {
  //    validatePrivate();
  //  }

  // лучше перенести в util
  public static String buildPrivateKey(Long u1, Long u2) {
    Objects.requireNonNull(u1);
    Objects.requireNonNull(u2);

    long min = Math.min(u1, u2);
    long max = Math.max(u1, u2);

    return min + "_" + max;
  }

  // =========================
  // EQUALS / HASHCODE
  // =========================

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;

    // instanceof → работает с Hibernate proxy
    if (!(o instanceof Chat that)) return false;

    // сравнение по id (если сущность уже сохранена)
    return id != null && id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return id != null ? id.hashCode() : System.identityHashCode(this);
  }

  // =========================
  // AGGREGATE LOGIC
  // =========================

  public boolean hasMember(Long userId) {
    return members.stream().anyMatch(m -> m.getUser().getId().equals(userId));
  }

  public void updateLastMessageTime() {
    // Обновление времени последнего сообщения
    // используется для сортировки чатов (inbox)
    this.lastMessageAt = Instant.now().truncatedTo(ChronoUnit.MILLIS);
  }

  public void setName(String name) {
    if (name != null && name.isBlank()) {
      throw new BadRequestException("Name must not be blank");
    }
    this.name = name != null ? name.trim() : null;
  }

  public Set<ChatMember> getMembers() {
    // Возвращаем read-only представление
    // → запрещаем внешнему коду модифицировать коллекцию напрямую
    return Collections.unmodifiableSet(members);
  }

  public static Chat createGroup(String name, User creator, List<User> users) {

    if (name == null || name.isBlank()) {
      throw new BadRequestException("Group name must not be blank");
    }

    Instant now = Instant.now();

    Chat chat = new Chat();
    chat.name = name.trim();
    chat.type = ChatType.GROUP;
    chat.createdAt = now;
    chat.lastMessageAt = now;

    // 👑 создатель
    chat.addMemberInternal(creator, ChatRole.ADMIN);

    // 👥 участники
    for (User user : users) {
      if (!user.getId().equals(creator.getId())) {
        chat.addMemberInternal(user, ChatRole.MEMBER);
      }
    }

    return chat;
  }

  public static Chat createPrivate(User userA, User userB) {

    if (userA.getId().equals(userB.getId())) {
      throw new BadRequestException("Cannot create chat with yourself");
    }

    Instant now = Instant.now();

    Chat chat = new Chat();
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
        throw new ConflictException("Private chat must have exactly 2 members");
      }
      if (privateKey == null) {
        throw new ConflictException("Private chat must have privateKey");
      }
    }
  }

  public void addMember(User user, Long currentUserId) {

    if (type == ChatType.PRIVATE) {
      throw new BadRequestException("Cannot add members to private chat");
    }

    ChatMember currentUser = getMember(currentUserId);
    if (currentUser == null) {
      throw new ForbiddenException("Not a member of this chat");
    }

    if (!currentUser.isAdmin()) {
      throw new ForbiddenException("Only admin can add members");
    }

    if (hasMember(user.getId())) {
      throw new ConflictException("User already in chat");
    }

    addMemberInternal(user, ChatRole.MEMBER);
  }

  private void addMemberInternal(User user, ChatRole role) {

    if (hasMember(user.getId())) {
      throw new ConflictException("User already in chat");
    }

    ChatMember member = ChatMember.of(user, role);
    member.setChat(this); // 🔥 CRITICAL

    members.add(member); // ✅ ДОБАВЛЯЕМ ТОТ ЖЕ ОБЪЕКТ
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

    if (type == ChatType.PRIVATE) {
      throw new BadRequestException("Cannot remove members from private chat");
    }

    ChatMember currentUser = getMember(currentUserId);
    if (currentUser == null) {
      throw new ForbiddenException("Not a member of this chat");
    }

    if (!currentUser.isAdmin()) {
      throw new ForbiddenException("Only admin can remove members");
    }

    if (targetUserId.equals(currentUserId)) {
      throw new BadRequestException("Cannot remove yourself");
    }

    ChatMember target = getMember(targetUserId);
    if (target == null) {
      throw new NotFoundException("Member not found");
    }

    if (target.isAdmin() && countAdmins() <= 1) {
      throw new ConflictException("Cannot remove last admin");
    }

    removeMemberInternal(target);
  }

  private void removeMemberInternal(ChatMember member) {
    if (!members.remove(member)) {
      throw new NotFoundException("Member not in this chat");
    }
    member.setChat(null);
  }

  public void ensureCanDelete(Long currentUserId) {

    ChatMember currentUser = getMember(currentUserId);

    if (currentUser == null) {
      throw new ForbiddenException("Not a member of this chat");
    }

    if (!this.isPrivate() && !currentUser.isAdmin()) {
      throw new ForbiddenException("Only admin can delete group chat");
    }
  }
}
