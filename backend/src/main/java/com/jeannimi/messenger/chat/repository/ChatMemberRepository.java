package com.jeannimi.messenger.chat.repository;

import com.jeannimi.messenger.chat.entity.ChatMember;
import com.jeannimi.messenger.chat.entity.ChatRole;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {

  boolean existsByChatIdAndUserId(Long chatId, Long userId);

  Optional<ChatMember> findByChatIdAndUserId(Long chatId, Long userId);

  long countByChatIdAndRole(Long chatId, ChatRole role);

  /*
      Первая страница
  */
  @Query(
      """
      SELECT c.id
      FROM ChatMember cm
      JOIN cm.chat c
      WHERE cm.user.id = :userId
      ORDER BY c.lastMessageAt DESC NULLS LAST,
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
            c.lastMessageAt < :cursorTime
            OR (
                c.lastMessageAt = :cursorTime
                AND c.id < :cursorId
            )
        )
      ORDER BY c.lastMessageAt DESC NULLS LAST,
               c.id DESC
      """)
  List<Long> findNextPageIds(
      @Param("userId") Long userId,
      @Param("cursorTime") Instant cursorTime,
      @Param("cursorId") Long cursorId,
      Pageable pageable);
}
