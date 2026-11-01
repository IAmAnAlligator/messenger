package com.jeannimi.messenger.application.notification.service;

import com.jeannimi.messenger.application.notification.dto.MessageNotification;
import com.jeannimi.messenger.application.port.out.EmailNotificationPort;
import com.jeannimi.messenger.application.port.out.WebPushNotificationPort;
import com.jeannimi.messenger.domain.notification.NotificationChannel;
import com.jeannimi.messenger.domain.notification.NotificationOutbox;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationOutboxWorker {

  private static final int BATCH_SIZE = 50;
  private static final int WEB_PUSH_MAX_ATTEMPTS = 2;
  private static final int EMAIL_MAX_ATTEMPTS = 1;
  private static final long WEB_PUSH_RETRY_DELAY_SECONDS = 5;

  private final NotificationOutboxService notificationOutboxService;
  private final WebPushNotificationPort webPushNotificationPort;
  private final EmailNotificationPort emailNotificationPort;

  public void processPendingNotifications() {
    List<NotificationOutbox> tasks =
        notificationOutboxService.claimBatch(BATCH_SIZE);

    for (NotificationOutbox task : tasks) {
      processTask(task);
    }
  }

  private void processTask(NotificationOutbox task) {
    int maxAttempts = switch (task.getChannel()) {
      case WEB_PUSH -> WEB_PUSH_MAX_ATTEMPTS;
      case EMAIL -> EMAIL_MAX_ATTEMPTS;
    };

    if (task.getAttempts() > maxAttempts) {
      notificationOutboxService.markAsDead(
          task.getId(),
          "Maximum delivery attempts exceeded"
      );
      return;
    }

    MessageNotification notification = toNotification(task);

    try {
      switch (task.getChannel()) {
        case WEB_PUSH -> webPushNotificationPort.send(notification);
        case EMAIL -> emailNotificationPort.send(notification);
      }

    } catch (Exception exception) {
      handleFailure(task, exception);
      return;
    }

    notificationOutboxService.markAsSent(task.getId());

    log.info(
        "Notification delivered: id={}, channel={}, attempt={}",
        task.getId().value(),
        task.getChannel(),
        task.getAttempts()
    );
  }

  private void handleFailure(
      NotificationOutbox task,
      Exception exception
  ) {
    String error = exception.getMessage() == null
        ? exception.getClass().getSimpleName()
        : exception.getMessage();

    log.warn(
        "Notification delivery failed: id={}, channel={}, attempt={}",
        task.getId().value(),
        task.getChannel(),
        task.getAttempts(),
        exception
    );

    if (task.getChannel() == NotificationChannel.WEB_PUSH
        && task.getAttempts() < WEB_PUSH_MAX_ATTEMPTS) {

      Instant nextAttemptAt =
          Instant.now().plusSeconds(WEB_PUSH_RETRY_DELAY_SECONDS);

      notificationOutboxService.scheduleRetry(
          task.getId(),
          nextAttemptAt,
          error
      );

      log.info(
          "Web Push retry scheduled: id={}, nextAttemptAt={}",
          task.getId().value(),
          nextAttemptAt
      );

      return;
    }

    notificationOutboxService.markAsDead(task.getId(), error);

    log.error(
        "Notification outbox task marked as DEAD: id={}, channel={}",
        task.getId().value(),
        task.getChannel()
    );
  }

  private MessageNotification toNotification(NotificationOutbox task) {
    return new MessageNotification(
        task.getEventId(),
        task.getMessageId(),
        task.getChatId(),
        task.getSenderUserId(),
        task.getSenderUsername(),
        List.of(task.getRecipientUserId())
    );
  }
}