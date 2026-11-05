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
    validateNotification(notification);

    if (!notificationEventRepository.tryRegister(notification.eventId())) {
      log.debug("Notification event already processed: {}", notification.eventId());
      return;
    }

    List<UserId> webPushRecipients = new ArrayList<>();
    List<UserId> emailRecipients = new ArrayList<>();

    findRecipients(notification, webPushRecipients, emailRecipients);

    enqueueNotifications(notification, webPushRecipients, emailRecipients);

    log.debug(
        "Notification event processed: eventId={}, webPushRecipients={}, emailRecipients={}",
        notification.eventId(),
        webPushRecipients.size(),
        emailRecipients.size());
  }

  private void findRecipients(
      MessageNotification notification,
      List<UserId> webPushRecipients,
      List<UserId> emailRecipients) {
    List<UserId> recipientUserIds =
        notification.recipientUserIds().stream()
            .filter(userId -> !userId.equals(notification.senderId()))
            .distinct()
            .toList();

    if (recipientUserIds.isEmpty()) {
      return;
    }

    List<UserNotificationSettings> settingsList =
        settingsRepository.findAllByUserIds(recipientUserIds);

    for (UserNotificationSettings settings : settingsList) {
      addRecipientToNotificationLists(settings, notification, webPushRecipients, emailRecipients);
    }
  }

  private void addRecipientToNotificationLists(
      UserNotificationSettings settings,
      MessageNotification notification,
      List<UserId> webPushRecipients,
      List<UserId> emailRecipients) {
    UserId userId = settings.getUserId();

    boolean webPushEnabled = isWebPushEnabled(settings, notification.chatType());

    boolean emailEnabled = isEmailEnabled(settings, notification.chatType());

    if (!webPushEnabled && !emailEnabled) {
      return;
    }

    if (!userPresenceService.isOnline(userId)) {
      if (emailEnabled) {
        emailRecipients.add(userId);
      }
      return;
    }

    if (webPushEnabled && !userPresenceService.isViewingChat(userId, notification.chatId())) {
      webPushRecipients.add(userId);
    }
  }

  private void enqueueNotifications(
      MessageNotification notification,
      List<UserId> webPushRecipients,
      List<UserId> emailRecipients) {
    if (!webPushRecipients.isEmpty()) {
      notificationOutboxService.enqueue(
          createNotification(notification, webPushRecipients), NotificationChannel.WEB_PUSH);

      log.debug("Enqueued Web Push notifications for {} recipients", webPushRecipients.size());
    }

    if (!emailRecipients.isEmpty()) {
      notificationOutboxService.enqueue(
          createNotification(notification, emailRecipients), NotificationChannel.EMAIL);

      log.debug("Enqueued Email notifications for {} recipients", emailRecipients.size());
    }
  }

  private void validateNotification(MessageNotification notification) {
    Objects.requireNonNull(notification, "notification");
    Objects.requireNonNull(notification.eventId(), "eventId");
    Objects.requireNonNull(notification.messageId(), "messageId");
    Objects.requireNonNull(notification.chatId(), "chatId");
    Objects.requireNonNull(notification.chatType(), "chatType");
    Objects.requireNonNull(notification.senderId(), "senderId");
    Objects.requireNonNull(notification.senderUsername(), "senderUsername");
    Objects.requireNonNull(notification.recipientUserIds(), "recipientUserIds");

    if (notification.senderUsername().isBlank()) {
      throw new IllegalArgumentException("senderUsername must not be blank");
    }

    if (notification.recipientUserIds().stream().anyMatch(Objects::isNull)) {
      throw new IllegalArgumentException("recipientUserIds must not contain null elements");
    }
  }

  private boolean isWebPushEnabled(UserNotificationSettings settings, ChatType chatType) {
    return switch (chatType) {
      case PRIVATE -> settings.isWebPushPrivateChatsEnabled();
      case GROUP -> settings.isWebPushGroupChatsEnabled();
    };
  }

  private boolean isEmailEnabled(UserNotificationSettings settings, ChatType chatType) {
    return switch (chatType) {
      case PRIVATE -> settings.isEmailPrivateChatsEnabled();
      case GROUP -> settings.isEmailGroupChatsEnabled();
    };
  }

  private MessageNotification createNotification(
      MessageNotification original, List<UserId> recipients) {

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
