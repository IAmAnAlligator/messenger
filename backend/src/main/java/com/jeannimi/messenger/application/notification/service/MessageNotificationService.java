package com.jeannimi.messenger.application.notification.service;

import com.jeannimi.messenger.application.notification.dto.MessageNotification;
import com.jeannimi.messenger.application.port.in.MessageNotificationUseCase;
import com.jeannimi.messenger.application.port.out.NotificationEventRepositoryPort;
import com.jeannimi.messenger.application.port.out.UserNotificationSettingsRepositoryPort;
import com.jeannimi.messenger.application.presence.service.UserPresenceService;
import com.jeannimi.messenger.domain.chat.ChatType;
import com.jeannimi.messenger.domain.notification.NotificationChannel;
import com.jeannimi.messenger.domain.notification.UserNotificationSettings;
import com.jeannimi.messenger.domain.user.UserId;
import java.util.ArrayList;
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
  private final NotificationOutboxService notificationOutboxService;

  @Override
  @Transactional
  public void notifyMessageReceived(MessageNotification notification) {
    Objects.requireNonNull(notification, "notification");

    if (notificationEventRepository.exists(notification.eventId())) {
      log.debug(
          "Notification event already processed: {}",
          notification.eventId());
      return;
    }

    List<UserId> webPushRecipients = new ArrayList<>();
    List<UserId> emailRecipients = new ArrayList<>();

    notification.recipientUserIds().stream()
        .filter(Objects::nonNull)
        .filter(userId -> !userId.equals(notification.senderId()))
        .distinct()
        .forEach(userId -> {
          UserNotificationSettings settings =
              settingsRepository.findByUserId(userId).orElse(null);

          if (settings == null) {
            return;
          }

          boolean online = userPresenceService.isOnline(userId);

          if (online) {
            boolean viewingChat =
                userPresenceService.isViewingChat(userId, notification.chatId());

            if (!viewingChat && isWebPushEnabled(settings, notification.chatType())) {
              webPushRecipients.add(userId);
            }
          } else if (isEmailEnabled(settings, notification.chatType())) {
            emailRecipients.add(userId);
          }
        });

    if (!webPushRecipients.isEmpty()) {
      notificationOutboxService.enqueue(
          createNotification(notification, webPushRecipients),
          NotificationChannel.WEB_PUSH);

      log.debug(
          "Enqueued Web Push notifications for {} recipients",
          webPushRecipients.size());
    }

    if (!emailRecipients.isEmpty()) {
      notificationOutboxService.enqueue(
          createNotification(notification, emailRecipients),
          NotificationChannel.EMAIL);

      log.debug(
          "Enqueued Email notifications for {} recipients",
          emailRecipients.size());
    }

    notificationEventRepository.save(notification.eventId());

    log.debug(
        "Notification event processed: eventId={}, webPushRecipients={}, emailRecipients={}",
        notification.eventId(),
        webPushRecipients.size(),
        emailRecipients.size());
  }

  private boolean isWebPushEnabled(
      UserNotificationSettings settings,
      ChatType chatType) {

    return switch (chatType) {
      case PRIVATE -> settings.isWebPushPrivateChatsEnabled();
      case GROUP -> settings.isWebPushGroupChatsEnabled();
    };
  }

  private boolean isEmailEnabled(
      UserNotificationSettings settings,
      ChatType chatType) {

    return switch (chatType) {
      case PRIVATE -> settings.isEmailPrivateChatsEnabled();
      case GROUP -> settings.isEmailGroupChatsEnabled();
    };
  }

  private MessageNotification createNotification(
      MessageNotification original,
      List<UserId> recipients) {

    return new MessageNotification(
        original.eventId(),
        original.messageId(),
        original.chatId(),
        original.chatType(),
        original.chatName(),
        original.senderId(),
        original.senderUsername(),
        recipients);
  }
}