package com.jeannimi.messenger.application.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.jeannimi.messenger.application.notification.dto.MessageNotification;
import com.jeannimi.messenger.application.port.out.NotificationEventRepositoryPort;
import com.jeannimi.messenger.application.port.out.UserNotificationSettingsRepositoryPort;
import com.jeannimi.messenger.application.presence.service.UserPresenceService;
import com.jeannimi.messenger.domain.chat.ChatId;
import com.jeannimi.messenger.domain.chat.ChatType;
import com.jeannimi.messenger.domain.common.EventId;
import com.jeannimi.messenger.domain.message.MessageId;
import com.jeannimi.messenger.domain.notification.NotificationChannel;
import com.jeannimi.messenger.domain.notification.UserNotificationSettings;
import com.jeannimi.messenger.domain.user.UserId;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MessageNotificationServiceTest {

  private static final UserId SENDER_ID = userId("00000000-0000-4000-8000-000000000001");

  private static final UserId RECIPIENT_ID = userId("00000000-0000-4000-8000-000000000002");

  private static final UserId SECOND_RECIPIENT_ID = userId("00000000-0000-4000-8000-000000000003");

  private static final ChatId CHAT_ID =
      new ChatId(UUID.fromString("00000000-0000-4000-8000-000000000004"));

  private static final MessageId MESSAGE_ID =
      new MessageId(UUID.fromString("00000000-0000-4000-8000-000000000005"));

  private static final EventId EVENT_ID =
      new EventId(UUID.fromString("00000000-0000-4000-8000-000000000006"));

  @Mock private NotificationEventRepositoryPort notificationEventRepository;

  @Mock private UserNotificationSettingsRepositoryPort settingsRepository;

  @Mock private UserPresenceService userPresenceService;

  @Mock private NotificationOutboxService notificationOutboxService;

  @InjectMocks private MessageNotificationService service;

  // --------------------------------------------------
  // Idempotency
  // --------------------------------------------------

  @Test
  void shouldNotProcessAlreadyRegisteredEvent() {
    when(notificationEventRepository.tryRegister(EVENT_ID)).thenReturn(false);

    service.notifyMessageReceived(notification());

    verify(notificationEventRepository).tryRegister(EVENT_ID);
    verifyNoInteractions(settingsRepository);
    verifyNoInteractions(userPresenceService);
    verifyNoInteractions(notificationOutboxService);
  }

  // --------------------------------------------------
  // Recipient filtering
  // --------------------------------------------------

  @Test
  void shouldExcludeSenderFromRecipients() {
    registerEvent();

    UserNotificationSettings settings = webPushPrivateSettings(RECIPIENT_ID);

    when(settingsRepository.findAllByUserIds(anyList())).thenReturn(List.of(settings));

    when(userPresenceService.isOnline(RECIPIENT_ID)).thenReturn(true);

    when(userPresenceService.isViewingChat(RECIPIENT_ID, CHAT_ID)).thenReturn(false);

    service.notifyMessageReceived(notification(List.of(SENDER_ID, RECIPIENT_ID)));

    ArgumentCaptor<List<UserId>> captor = ArgumentCaptor.captor();

    verify(settingsRepository).findAllByUserIds(captor.capture());

    assertEquals(List.of(RECIPIENT_ID), captor.getValue());

    verify(notificationOutboxService)
        .enqueue(any(MessageNotification.class), eq(NotificationChannel.WEB_PUSH));
  }

  @Test
  void shouldRemoveDuplicateRecipients() {
    registerEvent();

    when(settingsRepository.findAllByUserIds(anyList())).thenReturn(List.of());

    service.notifyMessageReceived(
        notification(
            List.of(RECIPIENT_ID, RECIPIENT_ID, SECOND_RECIPIENT_ID, SECOND_RECIPIENT_ID)));

    ArgumentCaptor<List<UserId>> captor = ArgumentCaptor.captor();

    verify(settingsRepository).findAllByUserIds(captor.capture());

    assertEquals(List.of(RECIPIENT_ID, SECOND_RECIPIENT_ID), captor.getValue());

    verifyNoInteractions(notificationOutboxService);
  }

  @Test
  void shouldNotLoadSettingsWhenRecipientListIsEmpty() {
    registerEvent();

    service.notifyMessageReceived(notification(List.of()));

    verify(notificationEventRepository).tryRegister(EVENT_ID);

    verifyNoInteractions(settingsRepository);
    verifyNoInteractions(userPresenceService);
    verifyNoInteractions(notificationOutboxService);
  }

  @Test
  void shouldNotNotifyUsersWithoutSettings() {
    registerEvent();

    when(settingsRepository.findAllByUserIds(anyList())).thenReturn(List.of());

    service.notifyMessageReceived(notification());

    verifyNoInteractions(notificationOutboxService);
    verifyNoInteractions(userPresenceService);
  }

  // --------------------------------------------------
  // Online recipients
  // --------------------------------------------------

  @Test
  void shouldEnqueueWebPushWhenUserIsOnlineAndNotViewingChat() {
    registerEvent();

    UserNotificationSettings settings = webPushPrivateSettings(RECIPIENT_ID);

    when(settingsRepository.findAllByUserIds(anyList())).thenReturn(List.of(settings));

    when(userPresenceService.isOnline(RECIPIENT_ID)).thenReturn(true);

    when(userPresenceService.isViewingChat(RECIPIENT_ID, CHAT_ID)).thenReturn(false);

    service.notifyMessageReceived(notification());

    verify(notificationOutboxService)
        .enqueue(any(MessageNotification.class), eq(NotificationChannel.WEB_PUSH));

    verify(userPresenceService).isViewingChat(RECIPIENT_ID, CHAT_ID);
  }

  @Test
  void shouldNotNotifyWhenUserIsViewingChat() {
    registerEvent();

    UserNotificationSettings settings = webPushPrivateSettings(RECIPIENT_ID);

    when(settingsRepository.findAllByUserIds(anyList())).thenReturn(List.of(settings));

    when(userPresenceService.isOnline(RECIPIENT_ID)).thenReturn(true);

    when(userPresenceService.isViewingChat(RECIPIENT_ID, CHAT_ID)).thenReturn(true);

    service.notifyMessageReceived(notification());

    verifyNoInteractions(notificationOutboxService);
  }

  @Test
  void shouldNotEnqueueWebPushWhenDisabled() {
    registerEvent();

    UserNotificationSettings settings = UserNotificationSettings.create(RECIPIENT_ID);
    settings.enableEmailPrivateChats(true);

    when(settingsRepository.findAllByUserIds(anyList())).thenReturn(List.of(settings));

    when(userPresenceService.isOnline(RECIPIENT_ID)).thenReturn(true);

    service.notifyMessageReceived(notification());

    verify(userPresenceService).isOnline(RECIPIENT_ID);
    verify(userPresenceService, never()).isViewingChat(any(), any());
    verifyNoInteractions(notificationOutboxService);
  }

  // --------------------------------------------------
  // Offline recipients
  // --------------------------------------------------

  @Test
  void shouldEnqueueEmailWhenUserIsOfflineAndEmailEnabled() {
    registerEvent();

    UserNotificationSettings settings = emailPrivateSettings(RECIPIENT_ID);

    when(settingsRepository.findAllByUserIds(anyList())).thenReturn(List.of(settings));

    when(userPresenceService.isOnline(RECIPIENT_ID)).thenReturn(false);

    service.notifyMessageReceived(notification());

    verify(notificationOutboxService)
        .enqueue(any(MessageNotification.class), eq(NotificationChannel.EMAIL));

    verify(userPresenceService, never()).isViewingChat(any(), any());
  }

  @Test
  void shouldNotEnqueueEmailWhenEmailIsDisabled() {
    registerEvent();

    UserNotificationSettings settings = UserNotificationSettings.create(RECIPIENT_ID);

    when(settingsRepository.findAllByUserIds(anyList())).thenReturn(List.of(settings));

    service.notifyMessageReceived(notification());

    verifyNoInteractions(notificationOutboxService);
  }

  @Test
  void shouldNotFallbackToEmailWhenOnlineAndWebPushIsDisabled() {
    registerEvent();

    UserNotificationSettings settings = UserNotificationSettings.create(RECIPIENT_ID);

    settings.enableEmailPrivateChats(true);

    when(settingsRepository.findAllByUserIds(anyList())).thenReturn(List.of(settings));

    when(userPresenceService.isOnline(RECIPIENT_ID)).thenReturn(true);

    service.notifyMessageReceived(notification());

    verifyNoInteractions(notificationOutboxService);
  }

  @Test
  void shouldNotFallbackToWebPushWhenOfflineAndEmailIsDisabled() {
    registerEvent();

    UserNotificationSettings settings = webPushPrivateSettings(RECIPIENT_ID);

    when(settingsRepository.findAllByUserIds(anyList())).thenReturn(List.of(settings));

    when(userPresenceService.isOnline(RECIPIENT_ID)).thenReturn(false);

    service.notifyMessageReceived(notification());

    verifyNoInteractions(notificationOutboxService);
  }

  // --------------------------------------------------
  // Multiple notification channels
  // --------------------------------------------------

  @Test
  void shouldEnqueueWebPushAndEmailForDifferentRecipients() {
    registerEvent();

    UserNotificationSettings onlineSettings = webPushPrivateSettings(RECIPIENT_ID);

    UserNotificationSettings offlineSettings = emailPrivateSettings(SECOND_RECIPIENT_ID);

    when(settingsRepository.findAllByUserIds(anyList()))
        .thenReturn(List.of(onlineSettings, offlineSettings));

    when(userPresenceService.isOnline(RECIPIENT_ID)).thenReturn(true);

    when(userPresenceService.isViewingChat(RECIPIENT_ID, CHAT_ID)).thenReturn(false);

    when(userPresenceService.isOnline(SECOND_RECIPIENT_ID)).thenReturn(false);

    service.notifyMessageReceived(notification(List.of(RECIPIENT_ID, SECOND_RECIPIENT_ID)));

    ArgumentCaptor<MessageNotification> captor = ArgumentCaptor.captor();

    verify(notificationOutboxService).enqueue(captor.capture(), eq(NotificationChannel.WEB_PUSH));

    assertEquals(List.of(RECIPIENT_ID), captor.getValue().recipientUserIds());

    verify(notificationOutboxService).enqueue(captor.capture(), eq(NotificationChannel.EMAIL));

    assertEquals(List.of(SECOND_RECIPIENT_ID), captor.getAllValues().get(1).recipientUserIds());

    verifyNoMoreInteractions(notificationOutboxService);
  }

  // --------------------------------------------------
  // Chat type settings
  // --------------------------------------------------

  @Test
  void shouldUsePrivateChatWebPushSettings() {
    registerEvent();

    UserNotificationSettings settings = UserNotificationSettings.create(RECIPIENT_ID);

    settings.enableWebPushPrivateChats(true);

    when(settingsRepository.findAllByUserIds(anyList())).thenReturn(List.of(settings));

    when(userPresenceService.isOnline(RECIPIENT_ID)).thenReturn(true);

    when(userPresenceService.isViewingChat(RECIPIENT_ID, CHAT_ID)).thenReturn(false);

    service.notifyMessageReceived(notification(ChatType.PRIVATE));

    verify(notificationOutboxService)
        .enqueue(any(MessageNotification.class), eq(NotificationChannel.WEB_PUSH));
  }

  @Test
  void shouldUseGroupChatWebPushSettings() {
    registerEvent();

    UserNotificationSettings settings = UserNotificationSettings.create(RECIPIENT_ID);

    settings.enableWebPushGroupChats(true);

    when(settingsRepository.findAllByUserIds(anyList())).thenReturn(List.of(settings));

    when(userPresenceService.isOnline(RECIPIENT_ID)).thenReturn(true);

    when(userPresenceService.isViewingChat(RECIPIENT_ID, CHAT_ID)).thenReturn(false);

    service.notifyMessageReceived(notification(ChatType.GROUP));

    verify(notificationOutboxService)
        .enqueue(any(MessageNotification.class), eq(NotificationChannel.WEB_PUSH));
  }

  @Test
  void shouldUseGroupChatEmailSettings() {
    registerEvent();

    UserNotificationSettings settings = UserNotificationSettings.create(RECIPIENT_ID);

    settings.enableEmailGroupChats(true);

    when(settingsRepository.findAllByUserIds(anyList())).thenReturn(List.of(settings));

    when(userPresenceService.isOnline(RECIPIENT_ID)).thenReturn(false);

    service.notifyMessageReceived(notification(ChatType.GROUP));

    verify(notificationOutboxService)
        .enqueue(any(MessageNotification.class), eq(NotificationChannel.EMAIL));
  }

  @Test
  void shouldNotUsePrivateSettingsForGroupChat() {
    registerEvent();

    UserNotificationSettings settings = webPushPrivateSettings(RECIPIENT_ID);

    when(settingsRepository.findAllByUserIds(anyList())).thenReturn(List.of(settings));

    service.notifyMessageReceived(notification(ChatType.GROUP));

    verifyNoInteractions(notificationOutboxService);
  }

  // --------------------------------------------------
  // Validation
  // --------------------------------------------------

  @Test
  void shouldThrowExceptionWhenNotificationIsNull() {
    assertThrows(NullPointerException.class, () -> service.notifyMessageReceived(null));

    verifyNoInteractions(notificationEventRepository);
    verifyNoInteractions(settingsRepository);
    verifyNoInteractions(notificationOutboxService);
  }

  @Test
  void shouldThrowExceptionWhenSenderUsernameIsBlank() {
    MessageNotification invalidNotification =
        new MessageNotification(
            EVENT_ID,
            MESSAGE_ID,
            CHAT_ID,
            ChatType.PRIVATE,
            null,
            SENDER_ID,
            " ",
            List.of(RECIPIENT_ID));

    assertThrows(
        IllegalArgumentException.class, () -> service.notifyMessageReceived(invalidNotification));

    verifyNoInteractions(notificationEventRepository);
    verifyNoInteractions(settingsRepository);
    verifyNoInteractions(notificationOutboxService);
  }

  @Test
  void shouldThrowExceptionWhenRecipientsContainNull() {
    MessageNotification invalidNotification =
        new MessageNotification(
            EVENT_ID,
            MESSAGE_ID,
            CHAT_ID,
            ChatType.PRIVATE,
            null,
            SENDER_ID,
            "sender",
            Arrays.asList(RECIPIENT_ID, null));

    assertThrows(
        IllegalArgumentException.class, () -> service.notifyMessageReceived(invalidNotification));

    verifyNoInteractions(notificationEventRepository);
    verifyNoInteractions(settingsRepository);
    verifyNoInteractions(notificationOutboxService);
  }

  // --------------------------------------------------
  // Notification payload
  // --------------------------------------------------

  @Test
  void shouldEnqueueNotificationWithCorrectRecipientAndEventData() {
    registerEvent();

    UserNotificationSettings settings = webPushPrivateSettings(RECIPIENT_ID);

    when(settingsRepository.findAllByUserIds(anyList())).thenReturn(List.of(settings));

    when(userPresenceService.isOnline(RECIPIENT_ID)).thenReturn(true);

    when(userPresenceService.isViewingChat(RECIPIENT_ID, CHAT_ID)).thenReturn(false);

    service.notifyMessageReceived(notification());

    ArgumentCaptor<MessageNotification> captor = ArgumentCaptor.captor();

    verify(notificationOutboxService).enqueue(captor.capture(), eq(NotificationChannel.WEB_PUSH));

    MessageNotification result = captor.getValue();

    assertEquals(EVENT_ID, result.eventId());
    assertEquals(MESSAGE_ID, result.messageId());
    assertEquals(CHAT_ID, result.chatId());
    assertEquals(ChatType.PRIVATE, result.chatType());
    assertEquals(SENDER_ID, result.senderId());
    assertEquals("sender", result.senderUsername());
    assertEquals(List.of(RECIPIENT_ID), result.recipientUserIds());
  }

  // --------------------------------------------------
  // Helpers
  // --------------------------------------------------

  private void registerEvent() {
    when(notificationEventRepository.tryRegister(EVENT_ID)).thenReturn(true);
  }

  private static UserId userId(String value) {
    return new UserId(UUID.fromString(value));
  }

  private MessageNotification notification() {
    return notification(ChatType.PRIVATE);
  }

  private MessageNotification notification(ChatType chatType) {
    return notification(chatType, List.of(RECIPIENT_ID));
  }

  private MessageNotification notification(List<UserId> recipients) {
    return notification(ChatType.PRIVATE, recipients);
  }

  private MessageNotification notification(ChatType chatType, List<UserId> recipients) {
    return new MessageNotification(
        EVENT_ID, MESSAGE_ID, CHAT_ID, chatType, null, SENDER_ID, "sender", recipients);
  }

  private UserNotificationSettings webPushPrivateSettings(UserId userId) {
    UserNotificationSettings settings = UserNotificationSettings.create(userId);

    settings.enableWebPushPrivateChats(true);

    return settings;
  }

  private UserNotificationSettings emailPrivateSettings(UserId userId) {
    UserNotificationSettings settings = UserNotificationSettings.create(userId);

    settings.enableEmailPrivateChats(true);

    return settings;
  }
}
