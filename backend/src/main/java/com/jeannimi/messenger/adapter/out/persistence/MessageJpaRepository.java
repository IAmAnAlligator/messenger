package com.jeannimi.messenger.adapter.out.persistence;

import com.jeannimi.messenger.message.entity.FileAttachment;
import com.jeannimi.messenger.message.entity.Message;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageJpaRepository extends JpaRepository<Message, Long> {

  @Query(
      """
    select m.attachment
    from Message m
    where m.chat.id = :chatId
    and m.attachment is not null
""")
  List<FileAttachment> findAttachmentsByChatId(@Param("chatId") Long chatId);

  List<Message> findAllByChatId(Long chatId, Pageable pageable);

  List<Message> findAllByChatId(Long chatId);

  @Query(
      """
    select m
    from Message m
    where m.id = :messageId
      and m.chat.id = :chatId
""")
  Optional<Message> findByIdAndChatId(
      @Param("messageId") Long messageId, @Param("chatId") Long chatId);

  @Query(
      """
SELECT m
FROM Message m
JOIN FETCH m.sender
WHERE m.chat.id = :chatId
ORDER BY m.createdAt DESC,
         m.id DESC
""")
  List<Message> findWithSenderByChatId(@Param("chatId") Long chatId, Pageable pageable);

  @Query(
      """
SELECT m
FROM Message m
JOIN FETCH m.sender
WHERE m.chat.id = :chatId
AND (
    m.createdAt < :createdAt
    OR
    (
        m.createdAt = :createdAt
        AND m.id < :id
    )
)
ORDER BY m.createdAt DESC, m.id DESC
""")
  List<Message> findWithSenderByChatIdAndCursor(
      @Param("chatId") Long chatId,
      @Param("createdAt") Instant createdAt,
      @Param("id") Long id,
      Pageable pageable);

  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query("""
    DELETE FROM Message m
    WHERE m.chat.id = :chatId
""")
  int deleteByChatId(@Param("chatId") Long chatId);

}
