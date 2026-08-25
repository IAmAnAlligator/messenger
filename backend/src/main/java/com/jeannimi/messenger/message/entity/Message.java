package com.jeannimi.messenger.message.entity;

import com.jeannimi.messenger.chat.entity.Chat;
import com.jeannimi.messenger.common.exception_handling.MessageError;
import com.jeannimi.messenger.common.exception_handling.MessageException;
import com.jeannimi.messenger.message.MessageConstants;
import com.jeannimi.messenger.user.entity.User;
import jakarta.persistence.CascadeType;
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
import jakarta.persistence.OneToOne;
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
      @Index(
          name = "idx_messages_chat_created_id",
          columnList = "chat_id, created_at DESC, id DESC"),
      @Index(name = "idx_messages_file_attachment_id", columnList = "file_attachment_id")
    })
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Message {

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

  @Column(name = "content", updatable = false, length = 2000)
  private String content;

  @Column(name = "created_at", nullable = false, updatable = false)
  private Instant createdAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false)
  private MessageType type;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
  @JoinColumn(name = "file_attachment_id")
  private FileAttachment attachment;

  @PrePersist
  private void prePersist() {
    if (createdAt == null) {
      createdAt = Instant.now();
    }
  }

  public static Message ofText(Chat chat, User sender, String content) {
    if (content == null || content.isBlank()) {
      throw new MessageException(MessageError.CONTENT_BLANK, "Message content must not be blank");
    }

    content = content.trim();

    if (content.length() > MessageConstants.MAX_CONTENT_LENGTH) {
      throw new MessageException(
          MessageError.CONTENT_TOO_LONG,
          "Message content exceeds " + MessageConstants.MAX_CONTENT_LENGTH + " characters");
    }

    Message message = new Message();

    message.chat = Objects.requireNonNull(chat, "chat");

    message.sender = Objects.requireNonNull(sender, "sender");

    message.content = content;

    message.type = MessageType.TEXT;

    //    message.status = MessageStatus.SENT;

    return message;
  }

  public static Message ofFile(Chat chat, User sender, FileAttachment attachment) {

    Message message = new Message();

    message.chat = Objects.requireNonNull(chat, "chat");

    message.sender = Objects.requireNonNull(sender, "sender");

    message.content = null;

    message.attachment = Objects.requireNonNull(attachment, "attachment");

    message.type = MessageType.FILE;
    //    message.status = MessageStatus.SENT;

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
