package com.jeannimi.messenger.application.notification.service;

import com.jeannimi.messenger.application.notification.dto.MessageNotification;
import com.jeannimi.messenger.application.port.in.MessageNotificationUseCase;
import com.jeannimi.messenger.application.port.out.NotificationEventRepositoryPort;
import com.jeannimi.messenger.application.port.out.UserNotificationSettingsRepositoryPort;
import com.jeannimi.messenger.application.port.out.WebPushNotificationPort;
import com.jeannimi.messenger.application.presence.service.UserPresenceService;
import com.jeannimi.messenger.domain.notification.UserNotificationSettings;
import com.jeannimi.messenger.domain.user.UserId;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MessageNotificationService implements MessageNotificationUseCase {

  private final NotificationEventRepositoryPort notificationEventRepository;
  private final UserNotificationSettingsRepositoryPort settingsRepository;
  private final UserPresenceService userPresenceService;
  private final WebPushNotificationPort webPushNotificationPort;

  @Override
  @Transactional
  public void notifyMessageReceived(MessageNotification notification) {
    Objects.requireNonNull(notification, "notification");

    if (notificationEventRepository.exists(notification.eventId())) {
      log.debug(
          "Notification event already processed: {}",
          notification.eventId()
      );
      return;
    }

    List<UserId> eligibleRecipients =
        notification.recipientUserIds().stream()
            .filter(userId -> !userId.equals(notification.senderId()))
            .filter(this::isWebPushEnabled)
            .filter(userPresenceService::isOnline)
            .filter(userId ->
                !userPresenceService.isViewingChat(
                    userId,
                    notification.chatId()
                )
            )
            .toList();

    if (!eligibleRecipients.isEmpty()) {
      MessageNotification pushNotification =
          new MessageNotification(
              notification.eventId(),
              notification.messageId(),
              notification.chatId(),
              notification.senderId(),
              notification.senderUsername(),
              eligibleRecipients
          );

      log.debug(
          "Sending Web Push to {} of {} recipients",
          eligibleRecipients.size(),
          notification.recipientUserIds().size()
      );

      webPushNotificationPort.send(pushNotification);
    } else {
      log.debug(
          "No eligible Web Push recipients for event {}",
          notification.eventId()
      );
    }

    notificationEventRepository.save(notification.eventId());
  }

  private boolean isWebPushEnabled(UserId userId) {
    return settingsRepository
        .findByUserId(userId)
        .map(UserNotificationSettings::isWebPushEnabled)
        .orElse(false);
  }
}