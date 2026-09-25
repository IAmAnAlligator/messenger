package com.jeannimi.messenger.adapter.out.persistence.mapper;

import com.jeannimi.messenger.adapter.out.persistence.entity.EmailJpaEntity;
import com.jeannimi.messenger.domain.user.Email;
import org.springframework.stereotype.Component;

@Component
public class EmailPersistenceMapper {

  public Email toDomain(EmailJpaEntity entity) {

    if (entity == null) {
      return null;
    }

    return new Email(entity.getValue());
  }

  public EmailJpaEntity toEntity(Email email) {

    if (email == null) {
      return null;
    }

    return new EmailJpaEntity(email.getValue());
  }
}