package com.jeannimi.messenger.adapter.out.persistence.entity;

import com.jeannimi.messenger.domain.chat.ChatRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chat_members")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMemberJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "chat_id", nullable = false)
  private ChatJpaEntity chat;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "user_id", nullable = false)
  private UserJpaEntity user;

  @Enumerated(EnumType.STRING)
  @Column(name = "role", nullable = false, length = 20)
  private ChatRole role;

  @Column(name = "joined_at", nullable = false)
  private Instant joinedAt;

  @Column(name = "last_read_message_id")
  private Long lastReadMessageId;

  public ChatMemberJpaEntity(
      Long id, UserJpaEntity user, ChatRole role, Instant joinedAt, Long lastReadMessageId) {

    this.id = id;
    this.user = user;
    this.role = role;
    this.joinedAt = joinedAt;
    this.lastReadMessageId = lastReadMessageId;
  }

  void setChat(ChatJpaEntity chat) {
    this.chat = chat;
  }

  public void updateLastReadMessageId(Long lastReadMessageId) {
    this.lastReadMessageId = lastReadMessageId;
  }
}
