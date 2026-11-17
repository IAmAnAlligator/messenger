package com.jeannimi.messenger.application.port.out.notification;

import com.jeannimi.messenger.application.notification.dto.MessageNotification;

public interface WebPushNotificationPort {

  void send(MessageNotification notification);
}
