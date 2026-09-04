package com.jeannimi.messenger.adapter.out.persistence;

import com.jeannimi.messenger.user.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserJpaRepository extends JpaRepository<User, Long> {

  Optional<User> findByUsername_ValueIgnoreCase(String username);

  boolean existsByUsername_ValueIgnoreCase(String username);

  @Query(
      """
        SELECT u FROM User u
        WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))
    """)
  List<User> searchByUsername(@Param("query") String query);

}
