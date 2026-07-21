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
    uniqueConstraints = @UniqueConstraint(columnNames = {"chat_id", "user_id"}),
    indexes = {
      @Index(name = "idx_chat_id", columnList = "chat_id"),
      @Index(name = "idx_user_id", columnList = "user_id")
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMember {

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "chat_id", nullable = false)
  @Setter(AccessLevel.PACKAGE)
  private Chat chat;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false)
  private ChatRole role;

  @Column(name = "joined_at", nullable = false)
  private Instant joinedAt;

  @PrePersist
  private void prePersist() {
    if (joinedAt == null) {
      joinedAt = Instant.now();
    }
  }

  public boolean isAdmin() {
    return role == ChatRole.ADMIN;
  }

  public void promoteToAdmin() {
    if (!isAdmin()) {
      role = ChatRole.ADMIN;
    }
  }

  public void demoteToMember() {
    if (isAdmin()) {
      role = ChatRole.MEMBER;
    }
  }

  public static ChatMember of(User user, ChatRole role) {
    ChatMember member = new ChatMember();
    member.user = Objects.requireNonNull(user, "user");
    member.role = Objects.requireNonNull(role, "role");
    return member;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (!(o instanceof ChatMember that)) return false;

    return id != null && id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }

}
