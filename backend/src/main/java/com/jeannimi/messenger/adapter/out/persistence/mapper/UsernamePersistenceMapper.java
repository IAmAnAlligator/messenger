package com.jeannimi.messenger.adapter.out.persistence.mapper;

import com.jeannimi.messenger.adapter.out.persistence.entity.UsernameJpaEntity;
import com.jeannimi.messenger.domain.user.Username;
import org.springframework.stereotype.Component;

@Component
public class UsernamePersistenceMapper {

  public Username toDomain(UsernameJpaEntity entity) {

    if (entity == null) {
      return null;
    }

    return new Username(entity.getValue());
  }

  public UsernameJpaEntity toEntity(Username username) {

    if (username == null) {
      return null;
    }

    return new UsernameJpaEntity(username);
  }
}
