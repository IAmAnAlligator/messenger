package com.jeannimi.messenger.repository;

import com.jeannimi.messenger.entity.Chat;
import com.jeannimi.messenger.entity.ChatMember;
import com.jeannimi.messenger.entity.ChatRole;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ChatMemberRepository extends JpaRepository<ChatMember, Long> {

  @Query(
      """
  SELECT DISTINCT c FROM ChatMember cm
  JOIN cm.chat c
  LEFT JOIN FETCH c.members m
  LEFT JOIN FETCH m.user
  WHERE cm.user.id = :userId
""")
  List<Chat> findChatsWithMembersByUserId(Long userId);

  boolean existsByChatIdAndUserId(Long chatId, Long userId);

  Optional<ChatMember> findByChatIdAndUserId(Long chatId, Long userId);

  long countByChatIdAndRole(Long chatId, ChatRole role);
}
