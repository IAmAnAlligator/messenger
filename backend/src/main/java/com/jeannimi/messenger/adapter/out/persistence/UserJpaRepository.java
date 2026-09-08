package com.jeannimi.messenger.adapter.out.persistence;

import com.jeannimi.messenger.adapter.out.persistence.entity.UserJpaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {

  Optional<UserJpaEntity> findByUsername_ValueIgnoreCase(String username);

  boolean existsByUsername_ValueIgnoreCase(String username);

  @Query(
      """
        SELECT u FROM UserJpaEntity u
        WHERE LOWER(u.username.value) LIKE LOWER(CONCAT('%', :query, '%'))
      """)
  List<UserJpaEntity> searchByUsername(@Param("query") String query);
}
