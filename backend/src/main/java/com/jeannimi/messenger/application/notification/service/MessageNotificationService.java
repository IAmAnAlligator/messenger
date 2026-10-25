package com.jeannimi.messenger.application.notification.service;

import com.jeannimi.messenger.application.notification.dto.MessageNotification;
import com.jeannimi.messenger.application.port.in.MessageNotificationUseCase;
import com.jeannimi.messenger.application.port.out.EmailNotificationPort;
import com.jeannimi.messenger.application.port.out.NotificationEventRepositoryPort;
import com.jeannimi.messenger.application.port.out.UserNotificationSettingsRepositoryPort;
import com.jeannimi.messenger.application.port.out.WebPushNotificationPort;
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
  private final WebPushNotificationPort webPushNotificationPort;
  private final EmailNotificationPort emailNotificationPort;

  @Override
  @Transactional
  public void notifyMessageReceived(MessageNotification notification) {
    Objects.requireNonNull(notification, "notification");

    if (notificationEventRepository.exists(notification.eventId())) {
      return;
    }

    List<UserId> enabledRecipients =
        notification.recipientUserIds().stream().filter(this::isWebPushEnabled).toList();

    if (!enabledRecipients.isEmpty()) {
      MessageNotification pushNotification =
          new MessageNotification(
              notification.eventId(),
              notification.messageId(),
              notification.chatId(),
              notification.senderId(),
              notification.senderUsername(),
              enabledRecipients);

      log.debug(
          "Web Push enabled recipients: {} of {}",
          enabledRecipients.size(),
          notification.recipientUserIds().size());

      webPushNotificationPort.send(pushNotification);
    }

    emailNotificationPort.send(notification);
    notificationEventRepository.save(notification.eventId());
  }

  private boolean isWebPushEnabled(UserId userId) {
    return settingsRepository
        .findByUserId(userId)
        .map(UserNotificationSettings::isWebPushEnabled)
        .orElse(false);
  }
}
