package com.jeannimi.messenger.application.port.out;

import com.jeannimi.messenger.domain.user.User;
import com.jeannimi.messenger.domain.user.UserId;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface UserRepositoryPort {

  Optional<User> findByEmail(String email);

  boolean existsByEmail(String email);

  List<User> searchByUsername(String query, UserId currentUserId);

  List<User> searchByHandle(String handle, UserId currentUserId);

  User save(User user);

  Optional<User> findById(UserId userId);

  List<User> findAllById(Set<UserId> ids);
}
