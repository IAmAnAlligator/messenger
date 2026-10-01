package com.jeannimi.messenger.adapter.out.persistence;

import com.jeannimi.messenger.adapter.out.persistence.entity.UserJpaEntity;
import com.jeannimi.messenger.adapter.out.persistence.mapper.UserPersistenceMapper;
import com.jeannimi.messenger.application.port.out.UserRepositoryPort;
import com.jeannimi.messenger.domain.user.User;
import com.jeannimi.messenger.domain.user.UserId;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserRepository implements UserRepositoryPort {

  private final UserJpaRepository userJpaRepository;
  private final UserPersistenceMapper userPersistenceMapper;

  @Override
  public Optional<User> findByEmail(String email) {

    return userJpaRepository.findByEmail_Value(email).map(userPersistenceMapper::toDomain);
  }

  @Override
  public boolean existsByEmail(String email) {

    return userJpaRepository.existsByEmail_Value(email);
  }

  @Override
  public List<User> searchByUsername(String query, UserId currentUserId) {

    return userJpaRepository.searchByUsername(query, currentUserId.value()).stream()
        .map(userPersistenceMapper::toDomain)
        .toList();
  }

  @Override
  public List<User> searchByHandle(String handle, UserId currentUserId) {

    return userJpaRepository.searchByHandle(handle, currentUserId.value()).stream()
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
  public Optional<User> findById(UserId userId) {

    return userJpaRepository.findById(userId.value()).map(userPersistenceMapper::toDomain);
  }

  @Override
  public List<User> findAllById(Set<UserId> ids) {

    Set<UUID> uuidIds =
        ids.stream().map(UserId::value).collect(java.util.stream.Collectors.toSet());

    return userJpaRepository.findAllById(uuidIds).stream()
        .map(userPersistenceMapper::toDomain)
        .toList();
  }
}
