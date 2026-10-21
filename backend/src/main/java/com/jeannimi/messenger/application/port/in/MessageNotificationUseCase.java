package com.jeannimi.messenger.application.port.in;

import com.jeannimi.messenger.application.notification.dto.MessageNotification;

public interface MessageNotificationUseCase {

  void notifyMessageReceived(MessageNotification notification);
}
