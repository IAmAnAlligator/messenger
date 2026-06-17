package com.jeannimi.messenger.entity;

import com.jeannimi.messenger.domain.Username;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity // JPA-сущность → отображается на таблицу в БД
@Table(
    name = "users",
    indexes = {
      // Индекс для быстрого поиска пользователя по username (логин, авторизация)
      @Index(name = "idx_username", columnList = "username")
    })
@Getter // только геттеры → контролируем мутабельность вручную
@NoArgsConstructor // нужен JPA (рефлексия)
public class User {

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  // Первичный ключ (auto-increment в БД)
  private Long id;

  @Setter
  @Embedded
  @AttributeOverride(name = "value", column = @Column(name = "username", unique = true))
  private Username username;

  @Setter(AccessLevel.NONE)
  @Column(name = "password", nullable = false)
  // Хранится НЕ пароль, а его ХЕШ (bcrypt/argon2)
  // Setter закрыт → нельзя случайно записать plain-text пароль
  private String passwordHash;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false)
  @Setter(AccessLevel.NONE)
  // Роль пользователя (USER, ADMIN и т.д.)
  // STRING → безопасно при изменении enum (в отличие от ORDINAL)
  private Role role;

  @Column(name = "created_at", nullable = false, updatable = false)
  // Дата создания пользователя
  // updatable = false → нельзя изменить после сохранения
  private Instant createdAt;

  @PrePersist
  public void prePersist() {
    // lifecycle hook: вызывается перед INSERT
    // гарантирует, что createdAt всегда будет заполнен
    if (createdAt == null) {
      createdAt = Instant.now();
    }
  }

  // =========================
  // BUSINESS LOGIC (DDD)
  // =========================

  public boolean isUser() {
    // Удобный метод для проверки роли
    return role == Role.USER;
  }

  public void changeRole(Role newRole) {
    // Контролируем изменение роли через метод
    // можно добавить проверки (например: только ADMIN может менять роли)
    this.role = Objects.requireNonNull(newRole);
  }

  public void setPasswordHash(String encodedPassword) {
    // Устанавливаем ТОЛЬКО уже захешированный пароль
    // (хеширование должно происходить в сервисе)
    this.passwordHash = Objects.requireNonNull(encodedPassword);
  }

  public static User of(Username username, String encodedPassword, Role role) {
    // Фабричный метод → создаёт ВСЕГДА валидный объект
    // защищает от "new User()" с пустыми полями
    User user = new User();
    user.setUsername(username);
    user.setPasswordHash(encodedPassword);
    user.role = Objects.requireNonNull(role);
    return user;
  }

  // =========================
  // EQUALS / HASHCODE
  // =========================

  @Override
  public boolean equals(Object o) {
    if (this == o) return true; // быстрая проверка ссылки

    // instanceof вместо getClass()
    // → работает с Hibernate proxy
    if (!(o instanceof User that)) return false;

    // сравнение по id (если уже сохранён в БД)
    return id != null && id.equals(that.id);
  }

  @Override
  public int hashCode() {
    // НЕ используем id!
    // потому что он может быть null до persist
    return getClass().hashCode();
  }
}
