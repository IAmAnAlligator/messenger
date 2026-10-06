package com.jeannimi.messenger.adapter.out.persistence;

import com.jeannimi.messenger.adapter.out.persistence.entity.ChatJpaEntity;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ChatJpaRepository extends JpaRepository<ChatJpaEntity, UUID> {

  @EntityGraph(attributePaths = {"members", "members.user"})
  List<ChatJpaEntity> findAllByIdIn(List<UUID> ids);

  @EntityGraph(attributePaths = {"members", "members.user"})
  Optional<ChatJpaEntity> findChatWithMembersById(UUID id);

  @EntityGraph(attributePaths = {"members", "members.user"})
  Optional<ChatJpaEntity> findByPrivateKey(String privateKey);

  @Modifying
  @Query(
      value =
          """
        INSERT INTO chats (
            id,
            name,
            type,
            created_at,
            last_message_at,
            private_key
        )
        VALUES (
            :id,
            :name,
            :type,
            :createdAt,
            :lastMessageAt,
            :privateKey
        )
        ON CONFLICT (private_key) DO NOTHING
        """,
      nativeQuery = true)
  int insertPrivateChatIfAbsent(
      @Param("id") UUID id,
      @Param("name") String name,
      @Param("type") String type,
      @Param("createdAt") Instant createdAt,
      @Param("lastMessageAt") Instant lastMessageAt,
      @Param("privateKey") String privateKey);
}
