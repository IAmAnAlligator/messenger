package com.jeannimi.messenger.repository;

import com.jeannimi.messenger.entity.Message;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MessageRepository extends JpaRepository<Message, Long> {

  Optional<Message> findByIdAndChatId(Long messageId, Long chatId);

  @Query(
      """
  SELECT m FROM Message m
  JOIN FETCH m.sender
  WHERE m.chat.id = :chatId
  ORDER BY m.id DESC
""")
  List<Message> findWithSenderByChatId(@Param("chatId") Long chatId, Pageable pageable);

  @Query(
      """
  SELECT m FROM Message m
  JOIN FETCH m.sender
  WHERE m.chat.id = :chatId
    AND m.id < :cursor
  ORDER BY m.id DESC
""")
  List<Message> findWithSenderByChatIdAndCursor(
      @Param("chatId") Long chatId, @Param("cursor") Long cursor, Pageable pageable);

  @Modifying
  @Query("""
DELETE FROM Message m
WHERE m.chat.id = :chatId
""")
  int deleteByChatId(@Param("chatId") Long chatId);
}
