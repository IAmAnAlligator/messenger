package com.jeannimi.messenger.adapter.out.persistence;

import com.jeannimi.messenger.adapter.out.persistence.entity.FileAttachmentJpaEntity;
import com.jeannimi.messenger.adapter.out.persistence.entity.MessageJpaEntity;
import com.jeannimi.messenger.adapter.out.persistence.projection.LastMessageProjection;
import com.jeannimi.messenger.adapter.out.persistence.projection.LastMessageStatusProjection;
import com.jeannimi.messenger.adapter.out.persistence.projection.UnreadCountProjection;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageJpaRepository extends JpaRepository<MessageJpaEntity, UUID> {

  @Query(
      """
    SELECT m.attachment
    FROM MessageJpaEntity m
    WHERE m.chat.id = :chatId
      AND m.attachment IS NOT NULL
    """)
  List<FileAttachmentJpaEntity> findAttachmentsByChatId(@Param("chatId") UUID chatId);

  List<MessageJpaEntity> findAllByChat_Id(UUID chatId, Pageable pageable);

  List<MessageJpaEntity> findAllByChat_Id(UUID chatId);

  @Query(
      """
SELECT m FROM MessageJpaEntity m JOIN FETCH m.sender WHERE m.id = :messageId AND m.chat.id = :chatId
    """)
  Optional<MessageJpaEntity> findByIdAndChatId(
      @Param("messageId") UUID messageId, @Param("chatId") UUID chatId);

  @Query(
      """
SELECT m FROM MessageJpaEntity m JOIN FETCH m.sender WHERE m.chat.id = :chatId ORDER BY m.createdAt DESC, m.id DESC
      """)
  List<MessageJpaEntity> findWithSenderByChatId(@Param("chatId") UUID chatId, Pageable pageable);

  @Query(
      """
SELECT m FROM MessageJpaEntity m JOIN FETCH m.sender WHERE m.chat.id = :chatId AND ( m.createdAt < :createdAt OR ( m.createdAt = :createdAt AND m.id < :id ) ) ORDER BY m.createdAt DESC, m.id DESC
      """)
  List<MessageJpaEntity> findWithSenderByChatIdAndCursor(
      @Param("chatId") UUID chatId,
      @Param("createdAt") Instant createdAt,
      @Param("id") UUID id,
      Pageable pageable);

  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query("""
      DELETE FROM MessageJpaEntity m
      WHERE m.chat.id = :chatId
      """)
  int deleteByChatId(@Param("chatId") UUID chatId);

  @Query(
      value =
          """
          SELECT DISTINCT ON (m.chat_id)
              m.chat_id AS chatId,
              m.id AS messageId,
              m.sender_id AS senderId,
              u.handle AS senderHandle,
              u.username AS senderUsername,
              u.role AS senderRole,
              m.encrypted_content AS encryptedContent,
              m.created_at AS createdAt
          FROM messages m
          JOIN users u
            ON u.id = m.sender_id
          WHERE m.chat_id IN (:chatIds)
          ORDER BY
              m.chat_id,
              m.created_at DESC,
              m.id DESC
          """,
      nativeQuery = true)
  List<LastMessageProjection> findLastMessagesByChatIds(
      @Param("chatIds") List<UUID> chatIds);

  @Query(
      value =
          """
          SELECT
              cm.chat_id AS chatId,
              COUNT(m.id) AS unreadCount
          FROM chat_members cm
  
          LEFT JOIN messages m
            ON m.chat_id = cm.chat_id
           AND m.sender_id <> :userId
  
          LEFT JOIN messages last_read
            ON last_read.id = cm.last_read_message_id
           AND last_read.chat_id = cm.chat_id
  
          WHERE cm.user_id = :userId
            AND cm.chat_id IN (:chatIds)
  
            AND (
                cm.last_read_message_id IS NULL
  
                OR m.created_at > last_read.created_at
  
                OR (
                    m.created_at = last_read.created_at
                    AND m.id > last_read.id
                )
            )
  
          GROUP BY cm.chat_id
          """,
      nativeQuery = true)
  List<UnreadCountProjection> findUnreadCounts(
      @Param("userId") UUID userId,
      @Param("chatIds") List<UUID> chatIds);


  @Query(
      value =
          """
          SELECT
              lm.chat_id AS chatId,
  
              CASE
                  WHEN lm.sender_id <> :userId
                      THEN 'NONE'
  
                  WHEN EXISTS (
                      SELECT 1
                      FROM chat_members reader
                      JOIN messages read_message
                        ON read_message.id =
                           reader.last_read_message_id
                       AND read_message.chat_id =
                           lm.chat_id
  
                      WHERE reader.chat_id = lm.chat_id
                        AND reader.user_id <> :userId
  
                        AND (
                            read_message.created_at > lm.created_at
  
                            OR (
                                read_message.created_at = lm.created_at
                                AND read_message.id >= lm.id
                            )
                        )
                  )
                      THEN 'READ'
  
                  ELSE 'SENT'
              END AS status
  
          FROM (
              SELECT DISTINCT ON (m.chat_id)
                  m.chat_id,
                  m.id,
                  m.sender_id,
                  m.created_at
              FROM messages m
              WHERE m.chat_id IN (:chatIds)
              ORDER BY
                  m.chat_id,
                  m.created_at DESC,
                  m.id DESC
          ) lm
          """,
      nativeQuery = true)
  List<LastMessageStatusProjection> findLastMessageStatuses(
      @Param("userId") UUID userId,
      @Param("chatIds") List<UUID> chatIds);
}
