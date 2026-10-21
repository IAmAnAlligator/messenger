package com.jeannimi.messenger.application.port.out;

import com.jeannimi.messenger.domain.notification.UserNotificationSettings;
import com.jeannimi.messenger.domain.user.UserId;
import java.util.Optional;

public interface UserNotificationSettingsRepositoryPort {

  Optional<UserNotificationSettings> findByUserId(UserId userId);

  UserNotificationSettings save(UserNotificationSettings settings);
}
