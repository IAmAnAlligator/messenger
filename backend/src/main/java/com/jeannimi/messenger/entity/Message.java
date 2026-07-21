package com.jeannimi.messenger.entity;

import com.jeannimi.messenger.exception_handling.MessageError;
import com.jeannimi.messenger.exception_handling.MessageException;
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
import org.hibernate.annotations.BatchSize;

@BatchSize(size = 50)
@Entity
@Table(
    name = "messages",
    indexes = {
      @Index(name = "idx_chat_created", columnList = "chat_id, created_at DESC"),
      @Index(name = "idx_chat_id_id", columnList = "chat_id, id DESC")
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message {

  private static final int MAX_CONTENT_LENGTH = 2000;

  @Id
  @Column(name = "id")
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "chat_id", nullable = false)
  private Chat chat;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "sender_id", nullable = false)
  private User sender;

  @Column(name = "content", nullable = false, updatable = false, length = 2000)
  private String content;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false)
  private MessageStatus status;

  @PrePersist
  private void prePersist() {
    if (createdAt == null) {
      createdAt = Instant.now();
    }
  }

  public void markDelivered() {
    if (this.status == MessageStatus.SENT) {
      this.status = MessageStatus.DELIVERED;
    }
  }

  public void markRead() {
    if (this.status == MessageStatus.SENT || this.status == MessageStatus.DELIVERED) {
      this.status = MessageStatus.READ;
    }
  }

  public static Message of(Chat chat, User sender, String content) {
    if (content == null || content.isBlank()) {
      throw new MessageException(MessageError.CONTENT_BLANK, "Message content must not be blank");
    }

    content = content.trim();

    if (content.length() > MAX_CONTENT_LENGTH) {
      throw new MessageException(MessageError.CONTENT_TOO_LONG, "Message content exceeds " +
          MAX_CONTENT_LENGTH + " characters");
    }

    Message message = new Message();

    message.chat = Objects.requireNonNull(chat, "chat");

    message.sender = Objects.requireNonNull(sender, "sender");

    message.content = content;

    message.status = MessageStatus.SENT;

    return message;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;

    if (!(o instanceof Message that)) return false;

    return id != null && id.equals(that.id);
  }

  @Override
  public int hashCode() {
    return getClass().hashCode();
  }
}
