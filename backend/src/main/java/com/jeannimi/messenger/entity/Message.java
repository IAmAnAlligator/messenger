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
import java.time.Instant;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.BatchSize;

@BatchSize(size = 50)
// Hibernate-оптимизация:
// при загрузке LAZY-связей (chat, sender) будет подтягивать пачками по 50
// → уменьшает N+1 проблему
@Entity // JPA-сущность → таблица messages
@Table(
    name = "messages",
    indexes = {
      // Индекс для сортировки сообщений по времени внутри чата
      // используется для "загрузить последние сообщения"
      @Index(name = "idx_chat_created", columnList = "chat_id, created_at DESC"),

      // Индекс для cursor-based pagination (лучший вариант)
      // → WHERE chat_id = ? AND id < ? ORDER BY id DESC
      @Index(name = "idx_chat_id_id", columnList = "chat_id, id DESC")
    })
@Getter
@Setter // ⚠️ частично ограничен ниже (chat, sender, status)
@NoArgsConstructor(access = AccessLevel.PROTECTED) // нужен JPA
public class Message {

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  // Первичный ключ (auto-increment)
  // Используется также как cursor для pagination
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "chat_id", nullable = false)
  @Setter(AccessLevel.NONE)
  // Чат, к которому относится сообщение
  // LAZY → не загружаем чат каждый раз
  // Setter закрыт → нельзя менять чат после создания (immutable связь)
  private Chat chat;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sender_id", nullable = false)
  @Setter(AccessLevel.NONE)
  // Отправитель сообщения
  // LAZY → не грузим пользователя без необходимости
  // Setter закрыт → нельзя изменить отправителя
  private User sender;

  @Column(name = "content", nullable = false, updatable = false, length = 2000)
  // Текст сообщения
  // - nullable = false → обязательно
  // - updatable = false → нельзя изменить после создания (immutable)
  // - length → ограничение размера
  private String content;

  @Column(name = "created_at", nullable = false, updatable = false)
  // Время создания сообщения
  // immutable → важно для корректной сортировки и истории
  private Instant createdAt;

  @PrePersist
  public void prePersist() {
    // lifecycle hook перед INSERT
    // гарантирует, что createdAt всегда установлен
    if (createdAt == null) {
      createdAt = Instant.now();
    }
  }

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  @Setter(AccessLevel.NONE)
  // Статус сообщения:
  // SENT → DELIVERED → READ
  // Setter закрыт → изменение только через методы (контроль переходов состояний)
  private MessageStatus status;

  // =========================
  // BUSINESS LOGIC (STATE MACHINE)
  // =========================

  public void markDelivered() {
    // Переход состояния:
    // SENT → DELIVERED
    if (this.status == MessageStatus.SENT) {
      this.status = MessageStatus.DELIVERED;
    }
  }

  public void markRead() {
    // Переход состояния:
    // SENT → READ
    // DELIVERED → READ
    if (this.status == MessageStatus.SENT || this.status == MessageStatus.DELIVERED) {
      this.status = MessageStatus.READ;
    }
  }

  public static Message of(Chat chat, User sender, String content) {
    // Фабричный метод → гарантирует валидное состояние

    if (content == null || content.isBlank()) {
      throw new IllegalArgumentException("Content must not be blank");
    }

    Message message = new Message();

    // Обязательные связи
    message.chat = Objects.requireNonNull(chat);
    message.sender = Objects.requireNonNull(sender);

    // Контент
    message.content = Objects.requireNonNull(content);

    // Начальное состояние
    message.status = MessageStatus.SENT;

    return message;
  }

  // =========================
  // EQUALS / HASHCODE
  // =========================

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;

    // instanceof → работает с Hibernate proxy
    if (!(o instanceof Message that)) return false;

    // сравнение по id (если сущность уже сохранена)
    return id != null && id.equals(that.id);
  }

  @Override
  public int hashCode() {
    // не используем id → он может быть null до persist
    return getClass().hashCode();
  }
}
