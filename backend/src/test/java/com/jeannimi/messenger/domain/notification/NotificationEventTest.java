package com.jeannimi.messenger.domain.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jeannimi.messenger.domain.event.EventId;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class NotificationEventTest {

  private static final EventId EVENT_ID =
      new EventId(UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789001"));

  private static final EventId ANOTHER_EVENT_ID =
      new EventId(UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789002"));

  private static final Instant PROCESSED_AT = Instant.parse("2026-11-04T12:00:00Z");

  // create

  @Test
  void create_shouldCreateEventWithCurrentTimestamp() {
    Instant before = Instant.now();

    NotificationEvent event = NotificationEvent.create(EVENT_ID);

    Instant after = Instant.now();

    assertEquals(EVENT_ID, event.getId());
    assertNotNull(event.getProcessedAt());
    assertTrue(!event.getProcessedAt().isBefore(before) && !event.getProcessedAt().isAfter(after));
  }

  @Test
  void create_shouldThrowNullPointerException_whenIdIsNull() {
    assertThrows(NullPointerException.class, () -> NotificationEvent.create(null));
  }

  // reconstitute

  @Test
  void reconstitute_shouldRestoreEvent() {
    NotificationEvent event = NotificationEvent.reconstitute(EVENT_ID, PROCESSED_AT);

    assertEquals(EVENT_ID, event.getId());
    assertEquals(PROCESSED_AT, event.getProcessedAt());
  }

  @Test
  void reconstitute_shouldThrowNullPointerException_whenIdIsNull() {
    assertThrows(
        NullPointerException.class, () -> NotificationEvent.reconstitute(null, PROCESSED_AT));
  }

  @Test
  void reconstitute_shouldThrowNullPointerException_whenProcessedAtIsNull() {
    assertThrows(NullPointerException.class, () -> NotificationEvent.reconstitute(EVENT_ID, null));
  }

  // equals

  @Test
  void equals_shouldReturnTrue_whenEventsHaveSameId() {
    NotificationEvent first = NotificationEvent.reconstitute(EVENT_ID, PROCESSED_AT);

    NotificationEvent second =
        NotificationEvent.reconstitute(EVENT_ID, PROCESSED_AT.plusSeconds(60));

    assertEquals(first, second);
  }

  @Test
  void equals_shouldReturnFalse_whenEventsHaveDifferentIds() {
    NotificationEvent first = NotificationEvent.reconstitute(EVENT_ID, PROCESSED_AT);

    NotificationEvent second = NotificationEvent.reconstitute(ANOTHER_EVENT_ID, PROCESSED_AT);

    assertNotEquals(first, second);
  }

  @Test
  void equals_shouldReturnTrue_whenComparingSameInstance() {
    NotificationEvent event = NotificationEvent.reconstitute(EVENT_ID, PROCESSED_AT);

    assertEquals(event, event);
  }

  @Test
  void equals_shouldReturnFalse_whenComparingWithNull() {
    NotificationEvent event = NotificationEvent.reconstitute(EVENT_ID, PROCESSED_AT);

    assertNotEquals(null, event);
  }

  @Test
  void equals_shouldReturnFalse_whenComparingWithDifferentType() {
    NotificationEvent event = NotificationEvent.reconstitute(EVENT_ID, PROCESSED_AT);

    assertThat(event.equals("event")).isFalse();
  }

  // hashCode

  @Test
  void hashCode_shouldBeEqual_whenEventsHaveSameId() {
    NotificationEvent first = NotificationEvent.reconstitute(EVENT_ID, PROCESSED_AT);

    NotificationEvent second =
        NotificationEvent.reconstitute(EVENT_ID, PROCESSED_AT.plusSeconds(60));

    assertEquals(first.hashCode(), second.hashCode());
  }

  @Test
  void hashCode_shouldBeDifferent_whenEventsHaveDifferentIds() {
    NotificationEvent first = NotificationEvent.reconstitute(EVENT_ID, PROCESSED_AT);

    NotificationEvent second = NotificationEvent.reconstitute(ANOTHER_EVENT_ID, PROCESSED_AT);

    assertNotEquals(first.hashCode(), second.hashCode());
  }
}
