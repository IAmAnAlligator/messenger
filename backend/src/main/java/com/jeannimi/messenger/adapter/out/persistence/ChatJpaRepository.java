package com.jeannimi.messenger.adapter.out.persistence;

import com.jeannimi.messenger.chat.entity.Chat;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatJpaRepository
    extends JpaRepository<Chat, Long> {

  @Query(
      """
      SELECT DISTINCT c
      FROM Chat c
      LEFT JOIN FETCH c.members m
      LEFT JOIN FETCH m.user
      WHERE c.id IN :ids
      """)
  List<Chat> findByIdsWithMembers(
      @Param("ids") List<Long> ids);

  @Query(
      """
      SELECT DISTINCT c
      FROM Chat c
      JOIN FETCH c.members m
      JOIN FETCH m.user
      WHERE c.id = :chatId
      """)
  Optional<Chat> findByIdWithMembers(
      @Param("chatId") Long chatId);

  Optional<Chat> findByPrivateKey(String privateKey);
}
