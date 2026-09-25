package com.jeannimi.messenger.adapter.out.persistence;

import com.jeannimi.messenger.adapter.out.persistence.entity.UserJpaEntity;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserJpaRepository extends JpaRepository<UserJpaEntity, Long> {

  @Query(
      """
        SELECT u FROM UserJpaEntity u
        WHERE LOWER(u.username.value) LIKE LOWER(CONCAT('%', :query, '%'))
          AND u.id <> :currentUserId
      """)
  List<UserJpaEntity> searchByUsername(
      @Param("query") String query, @Param("currentUserId") Long currentUserId);

  Optional<UserJpaEntity> findByEmail_Value(String email);

  boolean existsByEmail_Value(String email);
}
