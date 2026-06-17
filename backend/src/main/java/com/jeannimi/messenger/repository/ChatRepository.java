package com.jeannimi.messenger.repository;

import com.jeannimi.messenger.entity.Chat;
import com.jeannimi.messenger.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatRepository extends JpaRepository<Chat, Long> {

  @Query(
      """
  SELECT DISTINCT c FROM Chat c
  JOIN FETCH c.members m
  JOIN FETCH m.user
  WHERE c.id = :chatId
""")
  Optional<Chat> findByIdWithMembers(Long chatId);

  @Query(
      """
  SELECT DISTINCT c FROM Chat c
  JOIN FETCH c.members m
  JOIN FETCH m.user
  WHERE c.id = :chatId
    AND EXISTS (
      SELECT 1 FROM ChatMember cm
      WHERE cm.chat = c AND cm.user.id = :userId
    )
""")
  Optional<Chat> findChatWithAccess(Long chatId, Long userId);

  @Query(
      """
SELECT c FROM Chat c
JOIN c.members m
WHERE c.type = 'PRIVATE'
  AND m.user.id IN (:user1, :user2)
GROUP BY c
HAVING COUNT(DISTINCT m.user.id) = 2
""")
  Optional<Chat> findPrivateChat(Long user1, Long user2);

  Optional<Chat> findByPrivateKey(String privateKey);

  List<Chat> findByMembersContaining(User user);

  @Query(
      """
    SELECT COUNT(c) > 0
    FROM Chat c
    WHERE c.type = 'PRIVATE'
      AND EXISTS (
          SELECT 1 FROM ChatMember m1
          WHERE m1.chat = c AND m1.user.id = :user1
      )
      AND EXISTS (
          SELECT 1 FROM ChatMember m2
          WHERE m2.chat = c AND m2.user.id = :user2
      )
""")
  boolean existsPrivateChatBetweenUsers(@Param("user1") Long user1, @Param("user2") Long user2);
}
