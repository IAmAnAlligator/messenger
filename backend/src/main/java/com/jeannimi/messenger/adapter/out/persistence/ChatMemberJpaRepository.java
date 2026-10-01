package com.jeannimi.messenger.adapter.out.persistence;

import com.jeannimi.messenger.adapter.out.persistence.entity.ChatMemberJpaEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMemberJpaRepository extends JpaRepository<ChatMemberJpaEntity, UUID> {

  List<ChatMemberJpaEntity> findAllByChat_Id(UUID chatId);

  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query(
      value =
          """
          UPDATE chat_members cm
          SET last_read_message_id = :lastReadMessageId
          FROM messages new_message
          WHERE cm.chat_id = :chatId
          AND cm.user_id = :userId
          AND new_message.id = :lastReadMessageId
          AND new_message.chat_id = :chatId
          AND (
            cm.last_read_message_id IS NULL
            OR NOT EXISTS (
              SELECT 1
              FROM messages old_message
              WHERE old_message.id = cm.last_read_message_id
              AND old_message.chat_id = :chatId
            )
            OR EXISTS (
              SELECT 1
              FROM messages old_message
              WHERE old_message.id = cm.last_read_message_id
              AND old_message.chat_id = :chatId
              AND (
                old_message.created_at < new_message.created_at
                OR (
                  old_message.created_at = new_message.created_at
                  AND old_message.id < new_message.id
                )
              )
            )
          )
          """,
      nativeQuery = true)
  int updateLastReadMessageId(
      @Param("chatId") UUID chatId,
      @Param("userId") UUID userId,
      @Param("lastReadMessageId") UUID lastReadMessageId);

  Optional<ChatMemberJpaEntity> findByChat_IdAndUser_Id(UUID chatId, UUID userId);

  boolean existsByChat_IdAndUser_Id(UUID chatId, UUID userId);

  @Query(
      """
      SELECT c.id
      FROM ChatMemberJpaEntity cm
      JOIN cm.chat c
      WHERE cm.user.id = :userId
      ORDER BY COALESCE(c.lastMessageAt, c.createdAt) DESC,
      c.id DESC
      """)
  List<UUID> findFirstPageIds(@Param("userId") UUID userId, Pageable pageable);

  @Query(
      """
      SELECT c.id
      FROM ChatMemberJpaEntity cm
      JOIN cm.chat c
      WHERE cm.user.id = :userId
      AND (
        COALESCE(c.lastMessageAt, c.createdAt) < :cursorTime
        OR (
          COALESCE(c.lastMessageAt, c.createdAt) = :cursorTime
          AND c.id < :cursorId
        )
      )
      ORDER BY COALESCE(c.lastMessageAt, c.createdAt) DESC,
      c.id DESC
      """)
  List<UUID> findNextPageIds(
      @Param("userId") UUID userId,
      @Param("cursorTime") Instant cursorTime,
      @Param("cursorId") UUID cursorId,
      Pageable pageable);
}
