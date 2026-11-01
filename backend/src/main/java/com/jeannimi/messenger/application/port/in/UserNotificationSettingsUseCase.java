package com.jeannimi.messenger.application.port.in;

import com.jeannimi.messenger.application.notification.dto.UserNotificationSettingsResult;
import com.jeannimi.messenger.domain.user.UserId;

public interface UserNotificationSettingsUseCase {

  UserNotificationSettingsResult getSettings(UserId userId);

  UserNotificationSettingsResult updateSettings(
      UserId userId,
      Boolean webPushEnabled,
      Boolean emailEnabled);
}