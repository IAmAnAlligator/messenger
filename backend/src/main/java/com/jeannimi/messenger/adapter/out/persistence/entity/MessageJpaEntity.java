package com.jeannimi.messenger.adapter.out.persistence.entity;

import com.jeannimi.messenger.domain.message.MessageType;
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
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.BatchSize;

@Entity
@Table(
    name = "messages",
    indexes = {
      @Index(
          name = "idx_messages_chat_created_id",
          columnList = "chat_id, created_at DESC, id DESC"),
      @Index(name = "idx_messages_file_attachment_id", columnList = "file_attachment_id")
    })
@BatchSize(size = 50)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MessageJpaEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "id")
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "chat_id", nullable = false)
  private ChatJpaEntity chat;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "sender_id", nullable = false)
  private UserJpaEntity sender;

  @Column(name = "content", length = 2000)
  private String content;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 20)
  private MessageType type;

  @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @JoinColumn(name = "file_attachment_id")
  private FileAttachmentJpaEntity attachment;

  public MessageJpaEntity(
      Long id,
      ChatJpaEntity chat,
      UserJpaEntity sender,
      String content,
      Instant createdAt,
      MessageType type,
      FileAttachmentJpaEntity attachment) {

    this.id = id;
    this.chat = chat;
    this.sender = sender;
    this.content = content;
    this.createdAt = createdAt;
    this.type = type;
    this.attachment = attachment;
  }
}
