package com.jeannimi.messenger.adapter.out.persistence.adapter;

import com.jeannimi.messenger.adapter.out.persistence.entity.UserNotificationSettingsJpaEntity;
import com.jeannimi.messenger.adapter.out.persistence.mapper.UserNotificationSettingsPersistenceMapper;
import com.jeannimi.messenger.adapter.out.persistence.repository.UserNotificationSettingsJpaRepository;
import com.jeannimi.messenger.application.port.out.UserNotificationSettingsRepositoryPort;
import com.jeannimi.messenger.domain.notification.UserNotificationSettings;
import com.jeannimi.messenger.domain.user.UserId;
import java.util.Objects;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class UserNotificationSettingsPersistenceAdapter
    implements UserNotificationSettingsRepositoryPort {

  private final UserNotificationSettingsJpaRepository repository;
  private final UserNotificationSettingsPersistenceMapper mapper;

  @Override
  public Optional<UserNotificationSettings> findByUserId(UserId userId) {
    Objects.requireNonNull(userId, "userId");

    return repository.findByUserId(userId.value()).map(mapper::toDomain);
  }

  @Override
  public UserNotificationSettings save(UserNotificationSettings settings) {
    Objects.requireNonNull(settings, "settings");

    UserNotificationSettingsJpaEntity entity = mapper.toEntity(settings);
    UserNotificationSettingsJpaEntity saved = repository.save(entity);

    return mapper.toDomain(saved);
  }
}
