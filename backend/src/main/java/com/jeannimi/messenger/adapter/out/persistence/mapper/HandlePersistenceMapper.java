package com.jeannimi.messenger.adapter.out.persistence.mapper;

import com.jeannimi.messenger.adapter.out.persistence.entity.HandleJpaEntity;
import com.jeannimi.messenger.domain.user.Handle;
import org.springframework.stereotype.Component;

@Component
public class HandlePersistenceMapper {

  public Handle toDomain(HandleJpaEntity entity) {

    if (entity == null) {
      return null;
    }

    return new Handle(entity.getValue());
  }

  public HandleJpaEntity toEntity(Handle handle) {

    if (handle == null) {
      return null;
    }

    return new HandleJpaEntity(handle.getValue());
  }
}