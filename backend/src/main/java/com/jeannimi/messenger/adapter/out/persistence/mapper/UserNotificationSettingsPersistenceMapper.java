package com.jeannimi.messenger.adapter.out.persistence.mapper;

import com.jeannimi.messenger.adapter.out.persistence.entity.UserNotificationSettingsJpaEntity;
import com.jeannimi.messenger.domain.notification.UserNotificationSettings;
import com.jeannimi.messenger.domain.user.UserId;
import org.springframework.stereotype.Component;

@Component
public class UserNotificationSettingsPersistenceMapper {

  public UserNotificationSettings toDomain(UserNotificationSettingsJpaEntity entity) {
    if (entity == null) {
      return null;
    }

    return UserNotificationSettings.reconstitute(
        new UserId(entity.getUserId()),
        entity.isWebPushEnabled(),
        entity.getCreatedAt(),
        entity.getUpdatedAt());
  }

  public UserNotificationSettingsJpaEntity toEntity(UserNotificationSettings settings) {

    if (settings == null) {
      return null;
    }

    return new UserNotificationSettingsJpaEntity(
        settings.getUserId().value(),
        settings.isWebPushEnabled(),
        settings.getCreatedAt(),
        settings.getUpdatedAt());
  }
}
