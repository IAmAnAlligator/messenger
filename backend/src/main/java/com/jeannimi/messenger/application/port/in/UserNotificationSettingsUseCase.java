package com.jeannimi.messenger.application.port.in;

import com.jeannimi.messenger.application.notification.dto.UserNotificationSettingsResult;
import com.jeannimi.messenger.domain.user.UserId;

public interface UserNotificationSettingsUseCase {

  UserNotificationSettingsResult getSettings(UserId userId);

  UserNotificationSettingsResult updateWebPushEnabled(UserId userId, boolean enabled);
}
