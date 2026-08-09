package com.jeannimi.messenger.chat.repository;

import com.jeannimi.messenger.chat.entity.ChatMember;
import java.time.Instant;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {

  boolean existsByChatIdAndUserId(Long chatId, Long userId);

  /*
      Первая страница
  */
  @Query(
      """
    SELECT c.id
    FROM ChatMember cm
    JOIN cm.chat c
    WHERE cm.user.id = :userId
    ORDER BY COALESCE(c.lastMessageAt, c.createdAt) DESC,
             c.id DESC
    """)
  List<Long> findFirstPageIds(@Param("userId") Long userId, Pageable pageable);

  /*
      Следующие страницы через cursor
  */
  @Query(
      """
    SELECT c.id
    FROM ChatMember cm
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
  List<Long> findNextPageIds(
      @Param("userId") Long userId,
      @Param("cursorTime") Instant cursorTime,
      @Param("cursorId") Long cursorId,
      Pageable pageable);
}
