package com.jeannimi.messenger.adapter.out.persistence;

import com.jeannimi.messenger.adapter.out.persistence.entity.ChatJpaEntity;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatJpaRepository extends JpaRepository<ChatJpaEntity, UUID> {

  @EntityGraph(attributePaths = {"members", "members.user"})
  List<ChatJpaEntity> findAllByIdIn(List<UUID> ids);

  @EntityGraph(attributePaths = {"members", "members.user"})
  Optional<ChatJpaEntity> findChatWithMembersById(UUID id);

  @EntityGraph(attributePaths = {"members", "members.user"})
  Optional<ChatJpaEntity> findByPrivateKey(String privateKey);
}
