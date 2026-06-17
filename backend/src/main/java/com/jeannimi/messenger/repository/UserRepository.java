package com.jeannimi.messenger.repository;

import com.jeannimi.messenger.domain.Username;
import com.jeannimi.messenger.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {

  Optional<User> findByUsername(Username username);

  boolean existsByUsername(Username username);
}
