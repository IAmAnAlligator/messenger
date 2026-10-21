package com.jeannimi.messenger.application.notification.service;

import com.jeannimi.messenger.application.notification.dto.UserNotificationSettingsResult;
import com.jeannimi.messenger.application.port.in.UserNotificationSettingsUseCase;
import com.jeannimi.messenger.application.port.out.UserNotificationSettingsRepositoryPort;
import com.jeannimi.messenger.domain.notification.UserNotificationSettings;
import com.jeannimi.messenger.domain.user.UserId;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserNotificationSettingsService implements UserNotificationSettingsUseCase {

  private final UserNotificationSettingsRepositoryPort settingsRepository;

  @Override
  @Transactional
  public UserNotificationSettingsResult getSettings(UserId userId) {
    Objects.requireNonNull(userId, "userId");

    UserNotificationSettings settings =
        settingsRepository
            .findByUserId(userId)
            .orElseGet(() -> settingsRepository.save(UserNotificationSettings.create(userId)));

    return toResult(settings);
  }

  @Override
  @Transactional
  public UserNotificationSettingsResult updateWebPushEnabled(UserId userId, boolean enabled) {

    Objects.requireNonNull(userId, "userId");

    UserNotificationSettings current =
        settingsRepository
            .findByUserId(userId)
            .orElseGet(() -> UserNotificationSettings.create(userId));

    UserNotificationSettings updated = current.withWebPushEnabled(enabled);

    UserNotificationSettings settings = settingsRepository.save(updated);

    return toResult(settings);
  }

  private UserNotificationSettingsResult toResult(UserNotificationSettings settings) {

    return new UserNotificationSettingsResult(
        settings.isWebPushEnabled(), settings.getCreatedAt(), settings.getUpdatedAt());
  }
}
