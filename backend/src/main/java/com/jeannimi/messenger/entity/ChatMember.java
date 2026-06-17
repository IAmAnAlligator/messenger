package com.jeannimi.messenger.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.Instant;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "chat_members",

    // Уникальность: один пользователь может быть в чате только один раз
    uniqueConstraints = @UniqueConstraint(columnNames = {"chat_id", "user_id"}),

    // Индексы для ускорения запросов:
    // - поиск участников чата
    // - поиск всех чатов пользователя
    indexes = {
      @Index(name = "idx_chat_id", columnList = "chat_id"),
      @Index(name = "idx_user_id", columnList = "user_id")
    })
@Getter
@Setter // ⚠️ частично ограничен ниже (setChat)
@NoArgsConstructor(access = AccessLevel.PROTECTED) // JPA требует
public class ChatMember {

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  // Первичный ключ
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "chat_id", nullable = false)
  @Setter(AccessLevel.PACKAGE)
  // Связь с чатом
  // LAZY → не грузим чат каждый раз
  // PACKAGE setter → только Chat управляет связью (aggregate root)
  private Chat chat;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  // Пользователь-участник
  // LAZY → не грузим User без необходимости
  @Setter(AccessLevel.NONE)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false)
  // Роль участника:
  // ADMIN / MEMBER
  // STRING → безопасно при изменении enum
  @Setter(AccessLevel.NONE)
  private ChatRole role;

  @Column(name = "joined_at", nullable = false)
  // Когда пользователь присоединился к чату
  private Instant joinedAt;

  @PrePersist
  public void prePersist() {
    // lifecycle hook перед INSERT
    // гарантирует заполнение joinedAt
    if (joinedAt == null) {
      joinedAt = Instant.now();
    }
  }

  // =========================
  // BUSINESS LOGIC
  // =========================

  public boolean isAdmin() {
    // Проверка роли
    return role == ChatRole.ADMIN;
  }

  public void promoteToAdmin() {
    // Повышение до администратора
    // ⚠️ в реальном приложении лучше проверять:
    // кто выполняет действие (например, только ADMIN)
    this.role = ChatRole.ADMIN;
  }

  public void demoteToMember() {
    // Понижение роли
    this.role = ChatRole.MEMBER;
  }

  public static ChatMember of(User user, ChatRole role) {
    // Фабричный метод
    // создаёт валидный объект без chat (его установит Chat.addMember())
    ChatMember member = new ChatMember();
    member.user = Objects.requireNonNull(user);
    member.role = Objects.requireNonNull(role);
    return member;
  }

  // =========================
  // EQUALS / HASHCODE
  // =========================

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ChatMember that)) return false;

    return user != null && user.getId() != null && user.getId().equals(that.user.getId());
  }

  @Override
  public int hashCode() {
    return Objects.hash(user != null ? user.getId() : null);
  }
}
