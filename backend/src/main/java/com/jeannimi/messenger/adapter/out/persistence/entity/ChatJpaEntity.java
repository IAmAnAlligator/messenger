package com.jeannimi.messenger.adapter.out.persistence.entity;

import com.jeannimi.messenger.domain.chat.ChatType;
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
import java.util.HashSet;
import java.util.Set;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "chats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 20)
  private ChatType type;

  @OneToMany(
      mappedBy = "chat",
      cascade = CascadeType.ALL,
      orphanRemoval = true,
      fetch = FetchType.LAZY)
  private Set<ChatMemberJpaEntity> members = new HashSet<>();

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "last_message_at")
  private Instant lastMessageAt;

  @Column(name = "private_key", unique = true, length = 255)
  private String privateKey;

  @Version
  @Column(name = "version")
  private Long version;

  public ChatJpaEntity(
      Long id,
      String name,
      ChatType type,
      Instant createdAt,
      Instant lastMessageAt,
      String privateKey) {

    this.id = id;
    this.name = name;
    this.type = type;
    this.createdAt = createdAt;
    this.lastMessageAt = lastMessageAt;
    this.privateKey = privateKey;
  }

  public void update(String name, Instant lastMessageAt) {

    this.name = name;
    this.lastMessageAt = lastMessageAt;
  }

  public void addMember(ChatMemberJpaEntity member) {

    members.add(member);
    member.setChat(this);
  }

  public void removeMember(ChatMemberJpaEntity member) {

    members.remove(member);
    member.setChat(null);
  }
}
