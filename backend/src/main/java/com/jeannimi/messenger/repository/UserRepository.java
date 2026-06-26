package com.jeannimi.messenger.repository;

import com.jeannimi.messenger.domain.Username;
import com.jeannimi.messenger.entity.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByUsername(Username username);

  boolean existsByUsername(Username username);

  @Query(
      """
        SELECT u FROM User u
        WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%'))
    """)
  List<User> searchByUsername(@Param("query") String query);
}
