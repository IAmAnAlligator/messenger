package com.jeannimi.messenger.adapter.out.persistence;

import com.jeannimi.messenger.adapter.out.persistence.entity.FileAttachmentJpaEntity;
import com.jeannimi.messenger.adapter.out.persistence.entity.MessageJpaEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageJpaRepository extends JpaRepository<MessageJpaEntity, Long> {

  @Query(
      """
      SELECT m.attachment
      FROM MessageJpaEntity m
      WHERE m.chat.id = :chatId
      AND m.attachment IS NOT NULL
      """)
  List<FileAttachmentJpaEntity> findAttachmentsByChatId(@Param("chatId") Long chatId);

  List<MessageJpaEntity> findAllByChat_Id(Long chatId, Pageable pageable);

  List<MessageJpaEntity> findAllByChat_Id(Long chatId);

  @Query(
      """
      SELECT m
      FROM MessageJpaEntity m
      WHERE m.id = :messageId
      AND m.chat.id = :chatId
      """)
  Optional<MessageJpaEntity> findByIdAndChatId(
      @Param("messageId") Long messageId, @Param("chatId") Long chatId);

  @Query(
      """
      SELECT m
      FROM MessageJpaEntity m
      JOIN FETCH m.sender
      WHERE m.chat.id = :chatId
      ORDER BY m.createdAt DESC,
               m.id DESC
      """)
  List<MessageJpaEntity> findWithSenderByChatId(@Param("chatId") Long chatId, Pageable pageable);

  @Query(
      """
      SELECT m
      FROM MessageJpaEntity m
      JOIN FETCH m.sender
      WHERE m.chat.id = :chatId
      AND (
          m.createdAt < :createdAt
          OR (
              m.createdAt = :createdAt
              AND m.id < :id
          )
      )
      ORDER BY m.createdAt DESC,
               m.id DESC
      """)
  List<MessageJpaEntity> findWithSenderByChatIdAndCursor(
      @Param("chatId") Long chatId,
      @Param("createdAt") Instant createdAt,
      @Param("id") Long id,
      Pageable pageable);

  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query("""
      DELETE FROM MessageJpaEntity m
      WHERE m.chat.id = :chatId
      """)
  int deleteByChatId(@Param("chatId") Long chatId);
}
