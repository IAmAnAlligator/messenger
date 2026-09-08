package com.jeannimi.messenger.adapter.out.persistence;

import com.jeannimi.messenger.adapter.out.persistence.entity.UserJpaEntity;
import com.jeannimi.messenger.adapter.out.persistence.mapper.UserPersistenceMapper;
import com.jeannimi.messenger.application.port.out.UserRepositoryPort;
import com.jeannimi.messenger.domain.user.User;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepository implements UserRepositoryPort {

  private final UserJpaRepository userJpaRepository;
  private final UserPersistenceMapper userPersistenceMapper;

  @Override
  public Optional<User> findByUsername_ValueIgnoreCase(String username) {

    return userJpaRepository
        .findByUsername_ValueIgnoreCase(username)
        .map(userPersistenceMapper::toDomain);
  }

  @Override
  public boolean existsByUsername_ValueIgnoreCase(String username) {

    return userJpaRepository.existsByUsername_ValueIgnoreCase(username);
  }

  @Override
  public List<User> searchByUsername(String query) {

    return userJpaRepository.searchByUsername(query).stream()
        .map(userPersistenceMapper::toDomain)
        .toList();
  }

  @Override
  public User save(User user) {

    UserJpaEntity entity = userPersistenceMapper.toEntity(user);

    UserJpaEntity saved = userJpaRepository.save(entity);

    return userPersistenceMapper.toDomain(saved);
  }

  @Override
  public Optional<User> findById(Long userId) {

    return userJpaRepository.findById(userId).map(userPersistenceMapper::toDomain);
  }

  @Override
  public List<User> findAllById(Set<Long> ids) {

    return userJpaRepository.findAllById(ids).stream()
        .map(userPersistenceMapper::toDomain)
        .toList();
  }
}
