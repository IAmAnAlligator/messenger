package com.jeannimi.messenger.application.notification.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.jeannimi.messenger.application.notification.dto.MessageNotification;
import com.jeannimi.messenger.application.port.out.IdGenerator;
import com.jeannimi.messenger.application.port.out.NotificationOutboxRepositoryPort;
import com.jeannimi.messenger.domain.chat.ChatId;
import com.jeannimi.messenger.domain.chat.ChatType;
import com.jeannimi.messenger.domain.common.EventId;
import com.jeannimi.messenger.domain.message.MessageId;
import com.jeannimi.messenger.domain.notification.NotificationChannel;
import com.jeannimi.messenger.domain.notification.NotificationOutbox;
import com.jeannimi.messenger.domain.notification.NotificationOutboxId;
import com.jeannimi.messenger.domain.user.UserId;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationOutboxServiceTest {

  private static final Instant FIXED_TIME = Instant.parse("2026-11-05T10:30:00Z");

  private static final UUID OUTBOX_UUID_1 = UUID.fromString("018f0000-0000-7000-8000-000000000001");

  private static final UUID OUTBOX_UUID_2 = UUID.fromString("018f0000-0000-7000-8000-000000000002");

  private static final UserId SENDER_ID =
      new UserId(UUID.fromString("018f0000-0000-7000-8000-000000000010"));

  private static final UserId RECIPIENT_ID_1 =
      new UserId(UUID.fromString("018f0000-0000-7000-8000-000000000011"));

  private static final UserId RECIPIENT_ID_2 =
      new UserId(UUID.fromString("018f0000-0000-7000-8000-000000000012"));

  private static final EventId EVENT_ID =
      new EventId(UUID.fromString("018f0000-0000-7000-8000-000000000020"));

  private static final MessageId MESSAGE_ID =
      new MessageId(UUID.fromString("018f0000-0000-7000-8000-000000000030"));

  private static final ChatId CHAT_ID =
      new ChatId(UUID.fromString("018f0000-0000-7000-8000-000000000040"));

  @Mock private IdGenerator idGenerator;

  @Mock private NotificationOutboxRepositoryPort notificationOutboxRepository;

  private final Clock clock = Clock.fixed(FIXED_TIME, ZoneOffset.UTC);

  private NotificationOutboxService notificationOutboxService;

  @BeforeEach
  void setUp() {
    notificationOutboxService =
        new NotificationOutboxService(idGenerator, notificationOutboxRepository, clock);
  }

  // --------------------------------------------------
  // enqueue()
  // --------------------------------------------------

  @Test
  void shouldEnqueueTaskForEachRecipient() {
    MessageNotification notification = createNotification(List.of(RECIPIENT_ID_1, RECIPIENT_ID_2));

    when(idGenerator.generate()).thenReturn(OUTBOX_UUID_1, OUTBOX_UUID_2);

    notificationOutboxService.enqueue(notification, NotificationChannel.WEB_PUSH);

    ArgumentCaptor<List<NotificationOutbox>> captor = ArgumentCaptor.forClass(List.class);

    verify(notificationOutboxRepository).saveAll(captor.capture());

    List<NotificationOutbox> tasks = captor.getValue();

    assertEquals(2, tasks.size());

    assertEquals(RECIPIENT_ID_1, tasks.get(0).getRecipientUserId());
    assertEquals(RECIPIENT_ID_2, tasks.get(1).getRecipientUserId());

    assertEquals(SENDER_ID, tasks.get(0).getSenderUserId());
    assertEquals(SENDER_ID, tasks.get(1).getSenderUserId());

    assertEquals(NotificationChannel.WEB_PUSH, tasks.get(0).getChannel());
    assertEquals(NotificationChannel.WEB_PUSH, tasks.get(1).getChannel());

    assertEquals(EVENT_ID, tasks.get(0).getEventId());
    assertEquals(MESSAGE_ID, tasks.get(0).getMessageId());
    assertEquals(CHAT_ID, tasks.get(0).getChatId());

    assertEquals("sender", tasks.get(0).getSenderUsername());

    verify(idGenerator, times(2)).generate();
  }

  @Test
  void shouldNotEnqueueTaskForSender() {
    MessageNotification notification = createNotification(List.of(SENDER_ID, RECIPIENT_ID_1));

    when(idGenerator.generate()).thenReturn(OUTBOX_UUID_1);

    notificationOutboxService.enqueue(notification, NotificationChannel.EMAIL);

    ArgumentCaptor<List<NotificationOutbox>> captor = ArgumentCaptor.forClass(List.class);

    verify(notificationOutboxRepository).saveAll(captor.capture());

    List<NotificationOutbox> tasks = captor.getValue();

    assertEquals(1, tasks.size());
    assertEquals(RECIPIENT_ID_1, tasks.get(0).getRecipientUserId());

    verify(idGenerator, times(1)).generate();
  }

  @Test
  void shouldRemoveDuplicateRecipients() {
    MessageNotification notification =
        createNotification(List.of(RECIPIENT_ID_1, RECIPIENT_ID_2, RECIPIENT_ID_1, RECIPIENT_ID_2));

    when(idGenerator.generate()).thenReturn(OUTBOX_UUID_1, OUTBOX_UUID_2);

    notificationOutboxService.enqueue(notification, NotificationChannel.WEB_PUSH);

    ArgumentCaptor<List<NotificationOutbox>> captor = ArgumentCaptor.forClass(List.class);

    verify(notificationOutboxRepository).saveAll(captor.capture());

    assertEquals(2, captor.getValue().size());
    verify(idGenerator, times(2)).generate();
  }

  @Test
  void shouldNotSaveTasksWhenThereAreNoRecipients() {
    MessageNotification notification = createNotification(List.of());

    notificationOutboxService.enqueue(notification, NotificationChannel.WEB_PUSH);

    verifyNoInteractions(notificationOutboxRepository);
    verifyNoInteractions(idGenerator);
  }

  @Test
  void shouldNotSaveTasksWhenOnlyRecipientIsSender() {
    MessageNotification notification = createNotification(List.of(SENDER_ID));

    notificationOutboxService.enqueue(notification, NotificationChannel.WEB_PUSH);

    verifyNoInteractions(notificationOutboxRepository);
    verifyNoInteractions(idGenerator);
  }

  @Test
  void shouldThrowExceptionWhenNotificationIsNull() {
    assertThrows(
        NullPointerException.class,
        () -> notificationOutboxService.enqueue(null, NotificationChannel.WEB_PUSH));

    verifyNoInteractions(notificationOutboxRepository);
    verifyNoInteractions(idGenerator);
  }

  @Test
  void shouldThrowExceptionWhenChannelIsNull() {
    MessageNotification notification = createNotification(List.of(RECIPIENT_ID_1));

    assertThrows(
        NullPointerException.class, () -> notificationOutboxService.enqueue(notification, null));

    verifyNoInteractions(notificationOutboxRepository);
    verifyNoInteractions(idGenerator);
  }

  // --------------------------------------------------
  // claimBatch()
  // --------------------------------------------------

  @Test
  void shouldReturnClaimedBatch() {
    List<NotificationOutbox> expected =
        List.of(mock(NotificationOutbox.class), mock(NotificationOutbox.class));

    when(notificationOutboxRepository.claimBatch(10)).thenReturn(expected);

    List<NotificationOutbox> result = notificationOutboxService.claimBatch(10);

    assertEquals(expected, result);

    verify(notificationOutboxRepository).claimBatch(10);
  }

  @Test
  void shouldThrowExceptionWhenBatchSizeIsZero() {
    assertThrows(IllegalArgumentException.class, () -> notificationOutboxService.claimBatch(0));

    verifyNoInteractions(notificationOutboxRepository);
  }

  @Test
  void shouldThrowExceptionWhenBatchSizeIsNegative() {
    assertThrows(IllegalArgumentException.class, () -> notificationOutboxService.claimBatch(-1));

    verifyNoInteractions(notificationOutboxRepository);
  }

  // --------------------------------------------------
  // markAsSent()
  // --------------------------------------------------

  @Test
  void shouldMarkTaskAsSent() {
    NotificationOutboxId id = new NotificationOutboxId(OUTBOX_UUID_1);

    notificationOutboxService.markAsSent(id);

    verify(notificationOutboxRepository).markAsSent(id);
  }

  @Test
  void shouldThrowExceptionWhenMarkAsSentIdIsNull() {
    assertThrows(NullPointerException.class, () -> notificationOutboxService.markAsSent(null));

    verifyNoInteractions(notificationOutboxRepository);
  }

  // --------------------------------------------------
  // scheduleRetry()
  // --------------------------------------------------

  @Test
  void shouldScheduleRetry() {
    NotificationOutboxId id = new NotificationOutboxId(OUTBOX_UUID_1);

    Instant nextAttemptAt = FIXED_TIME.plusSeconds(5);
    String error = "Push service unavailable";

    notificationOutboxService.scheduleRetry(id, nextAttemptAt, error);

    verify(notificationOutboxRepository).scheduleRetry(id, nextAttemptAt, error);
  }

  @Test
  void shouldThrowExceptionWhenScheduleRetryIdIsNull() {
    assertThrows(
        NullPointerException.class,
        () -> notificationOutboxService.scheduleRetry(null, FIXED_TIME, "error"));

    verifyNoInteractions(notificationOutboxRepository);
  }

  @Test
  void shouldThrowExceptionWhenNextAttemptAtIsNull() {
    NotificationOutboxId id = new NotificationOutboxId(OUTBOX_UUID_1);

    assertThrows(
        NullPointerException.class,
        () -> notificationOutboxService.scheduleRetry(id, null, "error"));

    verifyNoInteractions(notificationOutboxRepository);
  }

  @Test
  void shouldThrowExceptionWhenScheduleRetryErrorIsNull() {
    NotificationOutboxId id = new NotificationOutboxId(OUTBOX_UUID_1);

    assertThrows(
        NullPointerException.class,
        () -> notificationOutboxService.scheduleRetry(id, FIXED_TIME, null));

    verifyNoInteractions(notificationOutboxRepository);
  }

  @Test
  void shouldThrowExceptionWhenScheduleRetryErrorIsBlank() {
    NotificationOutboxId id = new NotificationOutboxId(OUTBOX_UUID_1);

    assertThrows(
        IllegalArgumentException.class,
        () -> notificationOutboxService.scheduleRetry(id, FIXED_TIME, "   "));

    verifyNoInteractions(notificationOutboxRepository);
  }

  // --------------------------------------------------
  // markAsDead()
  // --------------------------------------------------

  @Test
  void shouldMarkTaskAsDead() {
    NotificationOutboxId id = new NotificationOutboxId(OUTBOX_UUID_1);

    String error = "Maximum attempts exceeded";

    notificationOutboxService.markAsDead(id, error);

    verify(notificationOutboxRepository).markAsDead(id, error);
  }

  @Test
  void shouldThrowExceptionWhenMarkAsDeadIdIsNull() {
    assertThrows(
        NullPointerException.class, () -> notificationOutboxService.markAsDead(null, "error"));

    verifyNoInteractions(notificationOutboxRepository);
  }

  @Test
  void shouldThrowExceptionWhenMarkAsDeadErrorIsNull() {
    NotificationOutboxId id = new NotificationOutboxId(OUTBOX_UUID_1);

    assertThrows(NullPointerException.class, () -> notificationOutboxService.markAsDead(id, null));

    verifyNoInteractions(notificationOutboxRepository);
  }

  @Test
  void shouldThrowExceptionWhenMarkAsDeadErrorIsBlank() {
    NotificationOutboxId id = new NotificationOutboxId(OUTBOX_UUID_1);

    assertThrows(
        IllegalArgumentException.class, () -> notificationOutboxService.markAsDead(id, "   "));

    verifyNoInteractions(notificationOutboxRepository);
  }

  @Test
  void shouldThrowExceptionWhenRecipientListContainsNull() {
    MessageNotification notification =
        createNotification(Arrays.asList(RECIPIENT_ID_1, null, RECIPIENT_ID_2));

    assertThrows(
        IllegalArgumentException.class,
        () -> notificationOutboxService.enqueue(notification, NotificationChannel.WEB_PUSH));

    verifyNoInteractions(notificationOutboxRepository);
    verifyNoInteractions(idGenerator);
  }

  // --------------------------------------------------
  // Test data
  // --------------------------------------------------

  private MessageNotification createNotification(List<UserId> recipientUserIds) {
    return new MessageNotification(
        EVENT_ID,
        MESSAGE_ID,
        CHAT_ID,
        ChatType.PRIVATE,
        "Private chat",
        SENDER_ID,
        "sender",
        recipientUserIds);
  }
}
