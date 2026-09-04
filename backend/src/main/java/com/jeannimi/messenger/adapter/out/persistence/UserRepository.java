package com.jeannimi.messenger.adapter.out.persistence;

import com.jeannimi.messenger.application.port.out.UserRepositoryPort;
import com.jeannimi.messenger.user.entity.User;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepository implements UserRepositoryPort {

  private final UserJpaRepository userJpaRepository;

  @Override
  public Optional<User> findByUsername_ValueIgnoreCase(String username) {
    return userJpaRepository.findByUsername_ValueIgnoreCase(username);
  }

  @Override
  public boolean existsByUsername_ValueIgnoreCase(String username) {
    return userJpaRepository.existsByUsername_ValueIgnoreCase(username);
  }

  @Override
  public List<User> searchByUsername(String query) {
    return userJpaRepository.searchByUsername(query);
  }

  @Override
  public User save(User user) {
    return userJpaRepository.save(user);
  }

  @Override
  public Optional<User> findById(Long userId) {
    return userJpaRepository.findById(userId);
  }

  @Override
  public List<User> findAllById(Set<Long> ids) {
    return userJpaRepository.findAllById(ids);
  }


}
