package com.jeannimi.messenger.adapter.out.persistence;

import com.jeannimi.messenger.adapter.out.persistence.entity.ChatJpaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatJpaRepository extends JpaRepository<ChatJpaEntity, Long> {

  @EntityGraph(attributePaths = {"members", "members.user"})
  List<ChatJpaEntity> findAllByIdIn(List<Long> ids);

  @EntityGraph(attributePaths = {"members", "members.user"})
  Optional<ChatJpaEntity> findById(Long id);

  @EntityGraph(attributePaths = {"members", "members.user"})
  Optional<ChatJpaEntity> findByPrivateKey(String privateKey);
}
