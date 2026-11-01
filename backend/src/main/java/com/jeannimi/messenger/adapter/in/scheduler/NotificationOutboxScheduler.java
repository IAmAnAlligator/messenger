package com.jeannimi.messenger.adapter.in.scheduler;

import com.jeannimi.messenger.application.notification.service.NotificationOutboxWorker;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationOutboxScheduler {

  private final NotificationOutboxWorker notificationOutboxWorker;

  @Scheduled(fixedDelay = 1000)
  public void processNotificationOutbox() {
    notificationOutboxWorker.processPendingNotifications();
  }
}