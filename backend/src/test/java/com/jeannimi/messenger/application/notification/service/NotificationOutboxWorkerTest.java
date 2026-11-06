package com.jeannimi.messenger.application.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.jeannimi.messenger.application.notification.dto.MessageNotification;
import com.jeannimi.messenger.application.port.out.EmailNotificationPort;
import com.jeannimi.messenger.application.port.out.WebPushNotificationPort;
import com.jeannimi.messenger.domain.chat.ChatId;
import com.jeannimi.messenger.domain.chat.ChatType;
import com.jeannimi.messenger.domain.common.EventId;
import com.jeannimi.messenger.domain.message.MessageId;
import com.jeannimi.messenger.domain.notification.NotificationChannel;
import com.jeannimi.messenger.domain.notification.NotificationOutbox;
import com.jeannimi.messenger.domain.notification.NotificationOutboxId;
import com.jeannimi.messenger.domain.notification.NotificationOutboxStatus;
import com.jeannimi.messenger.domain.user.UserId;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationOutboxWorkerTest {

  private static final Instant FIXED_TIME = Instant.parse("2026-11-05T10:00:00Z");

  private static final Clock CLOCK = Clock.fixed(FIXED_TIME, ZoneOffset.UTC);

  private static final UUID OUTBOX_UUID_1 = UUID.fromString("10000000-0000-7000-8000-000000000001");

  private static final UUID OUTBOX_UUID_2 = UUID.fromString("10000000-0000-7000-8000-000000000002");

  private static final UUID EVENT_UUID = UUID.fromString("20000000-0000-7000-8000-000000000001");

  private static final UUID MESSAGE_UUID = UUID.fromString("30000000-0000-7000-8000-000000000001");

  private static final UUID CHAT_UUID = UUID.fromString("40000000-0000-7000-8000-000000000001");

  private static final UUID RECIPIENT_UUID =
      UUID.fromString("50000000-0000-7000-8000-000000000001");

  private static final UUID SENDER_UUID = UUID.fromString("60000000-0000-7000-8000-000000000001");

  private static final int MAX_ERROR_LENGTH = 1000;

  @Mock private NotificationOutboxService notificationOutboxService;

  @Mock private WebPushNotificationPort webPushNotificationPort;

  @Mock private EmailNotificationPort emailNotificationPort;

  private NotificationOutboxWorker worker;

  @BeforeEach
  void setUp() {
    worker =
        new NotificationOutboxWorker(
            notificationOutboxService, webPushNotificationPort, emailNotificationPort, CLOCK);
  }

  @Test
  void shouldSendWebPushAndMarkAsSent() {
    NotificationOutbox task = createTask(NotificationChannel.WEB_PUSH, 1, OUTBOX_UUID_1);

    when(notificationOutboxService.claimBatch(50)).thenReturn(List.of(task));

    worker.processPendingNotifications();

    verify(webPushNotificationPort).send(any(MessageNotification.class));
    verify(notificationOutboxService).markAsSent(task.getId());

    verifyNoInteractions(emailNotificationPort);
  }

  @Test
  void shouldSendEmailAndMarkAsSent() {
    NotificationOutbox task = createTask(NotificationChannel.EMAIL, 1, OUTBOX_UUID_1);

    when(notificationOutboxService.claimBatch(50)).thenReturn(List.of(task));

    worker.processPendingNotifications();

    verify(emailNotificationPort).send(any(MessageNotification.class));
    verify(notificationOutboxService).markAsSent(task.getId());

    verifyNoInteractions(webPushNotificationPort);
  }

  @Test
  void shouldScheduleWebPushRetryAfterFirstFailure() {
    NotificationOutbox task = createTask(NotificationChannel.WEB_PUSH, 1, OUTBOX_UUID_1);

    when(notificationOutboxService.claimBatch(50)).thenReturn(List.of(task));

    doThrow(new RuntimeException("Web Push unavailable"))
        .when(webPushNotificationPort)
        .send(any(MessageNotification.class));

    worker.processPendingNotifications();

    verify(notificationOutboxService)
        .scheduleRetry(task.getId(), FIXED_TIME.plusSeconds(5), "Web Push unavailable");

    verify(notificationOutboxService, never()).markAsSent(task.getId());
    verify(notificationOutboxService, never()).markAsDead(eq(task.getId()), any());
  }

  @Test
  void shouldMarkWebPushAsDeadAfterLastAttemptFails() {
    NotificationOutbox task = createTask(NotificationChannel.WEB_PUSH, 2, OUTBOX_UUID_1);

    when(notificationOutboxService.claimBatch(50)).thenReturn(List.of(task));

    doThrow(new RuntimeException("Web Push unavailable"))
        .when(webPushNotificationPort)
        .send(any(MessageNotification.class));

    worker.processPendingNotifications();

    verify(notificationOutboxService).markAsDead(task.getId(), "Web Push unavailable");

    verify(notificationOutboxService, never()).scheduleRetry(eq(task.getId()), any(), any());

    verify(notificationOutboxService, never()).markAsSent(task.getId());
  }

  @Test
  void shouldMarkEmailAsDeadAfterFailure() {
    NotificationOutbox task = createTask(NotificationChannel.EMAIL, 1, OUTBOX_UUID_1);

    when(notificationOutboxService.claimBatch(50)).thenReturn(List.of(task));

    doThrow(new RuntimeException("Email delivery failed"))
        .when(emailNotificationPort)
        .send(any(MessageNotification.class));

    worker.processPendingNotifications();

    verify(notificationOutboxService).markAsDead(task.getId(), "Email delivery failed");

    verify(notificationOutboxService, never()).scheduleRetry(eq(task.getId()), any(), any());

    verify(notificationOutboxService, never()).markAsSent(task.getId());
  }

  @Test
  void shouldMarkTaskAsDeadWhenMaximumAttemptsExceeded() {
    NotificationOutbox task = createTask(NotificationChannel.WEB_PUSH, 3, OUTBOX_UUID_1);

    when(notificationOutboxService.claimBatch(50)).thenReturn(List.of(task));

    worker.processPendingNotifications();

    verify(notificationOutboxService)
        .markAsDead(task.getId(), "Maximum delivery attempts exceeded");

    verifyNoInteractions(webPushNotificationPort);
    verifyNoInteractions(emailNotificationPort);
  }

  @Test
  void shouldTruncateLongErrorMessage() {
    NotificationOutbox task = createTask(NotificationChannel.EMAIL, 1, OUTBOX_UUID_1);

    when(notificationOutboxService.claimBatch(50)).thenReturn(List.of(task));

    String longMessage = "a".repeat(1500);

    doThrow(new RuntimeException(longMessage))
        .when(emailNotificationPort)
        .send(any(MessageNotification.class));

    worker.processPendingNotifications();

    ArgumentCaptor<String> errorCaptor = ArgumentCaptor.forClass(String.class);

    verify(notificationOutboxService).markAsDead(eq(task.getId()), errorCaptor.capture());

    assertEquals(MAX_ERROR_LENGTH, errorCaptor.getValue().length());
    assertEquals("a".repeat(MAX_ERROR_LENGTH), errorCaptor.getValue());
  }

  @Test
  void shouldUseExceptionClassNameWhenErrorMessageIsNull() {
    NotificationOutbox task = createTask(NotificationChannel.EMAIL, 1, OUTBOX_UUID_1);

    when(notificationOutboxService.claimBatch(50)).thenReturn(List.of(task));

    doThrow(new RuntimeException((String) null))
        .when(emailNotificationPort)
        .send(any(MessageNotification.class));

    worker.processPendingNotifications();

    verify(notificationOutboxService).markAsDead(task.getId(), "RuntimeException");
  }

  @Test
  void shouldUseExceptionClassNameWhenErrorMessageIsBlank() {
    NotificationOutbox task = createTask(NotificationChannel.EMAIL, 1, OUTBOX_UUID_1);

    when(notificationOutboxService.claimBatch(50)).thenReturn(List.of(task));

    doThrow(new RuntimeException("   "))
        .when(emailNotificationPort)
        .send(any(MessageNotification.class));

    worker.processPendingNotifications();

    verify(notificationOutboxService).markAsDead(task.getId(), "RuntimeException");
  }

  @Test
  void shouldContinueProcessingWhenOneTaskFails() {
    NotificationOutbox firstTask = createTask(NotificationChannel.WEB_PUSH, 1, OUTBOX_UUID_1);

    NotificationOutbox secondTask = createTask(NotificationChannel.EMAIL, 1, OUTBOX_UUID_2);

    when(notificationOutboxService.claimBatch(50)).thenReturn(List.of(firstTask, secondTask));

    doThrow(new RuntimeException("Database error"))
        .when(notificationOutboxService)
        .markAsSent(firstTask.getId());

    worker.processPendingNotifications();

    verify(webPushNotificationPort).send(any(MessageNotification.class));

    verify(emailNotificationPort).send(any(MessageNotification.class));

    verify(notificationOutboxService).markAsSent(firstTask.getId());

    verify(notificationOutboxService).markAsSent(secondTask.getId());
  }

  @Test
  void shouldSendRestoredWebPushTaskOnSecondAttempt() {
    // Task was previously PROCESSING and was recovered
    // after its lock expired.
    NotificationOutbox task = createTask(NotificationChannel.WEB_PUSH, 2, OUTBOX_UUID_1);

    when(notificationOutboxService.claimBatch(50)).thenReturn(List.of(task));

    worker.processPendingNotifications();

    verify(webPushNotificationPort).send(any(MessageNotification.class));

    verify(notificationOutboxService).markAsSent(task.getId());

    verify(notificationOutboxService, never()).markAsDead(eq(task.getId()), any());
  }

  private NotificationOutbox createTask(
      NotificationChannel channel, int attempts, UUID outboxUuid) {

    return NotificationOutbox.reconstitute(
        new NotificationOutboxId(outboxUuid),
        new EventId(EVENT_UUID),
        new MessageId(MESSAGE_UUID),
        new ChatId(CHAT_UUID),
        ChatType.PRIVATE,
        null,
        new UserId(RECIPIENT_UUID),
        new UserId(SENDER_UUID),
        "sender",
        channel,
        NotificationOutboxStatus.PROCESSING,
        attempts,
        FIXED_TIME,
        FIXED_TIME.plusSeconds(600),
        FIXED_TIME,
        null,
        null);
  }
}
