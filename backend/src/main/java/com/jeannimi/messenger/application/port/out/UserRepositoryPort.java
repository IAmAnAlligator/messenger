package com.jeannimi.messenger.application.port.out;

import com.jeannimi.messenger.user.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRepositoryPort {

  Optional<User> findByUsername_ValueIgnoreCase(String username);

  boolean existsByUsername_ValueIgnoreCase(String username);

  List<User> searchByUsername(String query);

  User save(User user);

  Optional<User> findById(Long userId);

  List<User> findAllById(Set<Long> ids);
}
