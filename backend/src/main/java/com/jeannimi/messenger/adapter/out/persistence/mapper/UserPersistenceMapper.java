package com.jeannimi.messenger.adapter.out.persistence.mapper;

import com.jeannimi.messenger.adapter.out.persistence.entity.UserJpaEntity;
import com.jeannimi.messenger.domain.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserPersistenceMapper {

  private final HandlePersistenceMapper handlePersistenceMapper;
  private final UsernamePersistenceMapper usernamePersistenceMapper;
  private final EmailPersistenceMapper emailPersistenceMapper;

  public User toDomain(UserJpaEntity entity) {

    if (entity == null) {
      return null;
    }

    return User.reconstitute(
        entity.getId(),
        handlePersistenceMapper.toDomain(entity.getHandle()),
        usernamePersistenceMapper.toDomain(entity.getUsername()),
        emailPersistenceMapper.toDomain(entity.getEmail()),
        entity.getPassword(),
        entity.getRole(),
        entity.getCreatedAt());
  }

  public UserJpaEntity toEntity(User user) {

    if (user == null) {
      return null;
    }

    return new UserJpaEntity(
        handlePersistenceMapper.toEntity(user.getHandle()),
        usernamePersistenceMapper.toEntity(user.getUsername()),
        emailPersistenceMapper.toEntity(user.getEmail()),
        user.getPasswordHash(),
        user.getRole(),
        user.getCreatedAt());
  }
}