package com.jeannimi.messenger.application.port.out;

import com.jeannimi.messenger.domain.user.User;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRepositoryPort {

  Optional<User> findByUsernameIgnoreCase(String username);

  boolean existsByUsernameIgnoreCase(String username);

  List<User> searchByUsername(String query, Long currentUserId);

  User save(User user);

  Optional<User> findById(Long userId);

  List<User> findAllById(Set<Long> ids);
}
