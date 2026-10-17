package com.jeannimi.messenger.domain.event;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ProcessedEventTest {

  private static final EventId EVENT_ID =
      new EventId(UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789012"));

  private static final EventId ANOTHER_EVENT_ID =
      new EventId(UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789013"));

  private static final Instant PROCESSED_AT = Instant.parse("2026-10-17T12:00:00Z");

  @Test
  void create_shouldCreateProcessedEventWithGivenIdAndCurrentTime() {
    Instant before = Instant.now();

    ProcessedEvent event = ProcessedEvent.create(EVENT_ID);

    Instant after = Instant.now();

    assertEquals(EVENT_ID, event.getId());
    assertNotNull(event.getProcessedAt());
    assertTrue(!event.getProcessedAt().isBefore(before) && !event.getProcessedAt().isAfter(after));
  }

  @Test
  void create_shouldThrowNullPointerException_whenIdIsNull() {
    assertThrows(NullPointerException.class, () -> ProcessedEvent.create(null));
  }

  @Test
  void reconstitute_shouldCreateProcessedEventWithGivenValues() {
    ProcessedEvent event = ProcessedEvent.reconstitute(EVENT_ID, PROCESSED_AT);

    assertEquals(EVENT_ID, event.getId());
    assertEquals(PROCESSED_AT, event.getProcessedAt());
  }

  @Test
  void reconstitute_shouldThrowNullPointerException_whenIdIsNull() {
    assertThrows(NullPointerException.class, () -> ProcessedEvent.reconstitute(null, PROCESSED_AT));
  }

  @Test
  void reconstitute_shouldThrowNullPointerException_whenProcessedAtIsNull() {
    assertThrows(NullPointerException.class, () -> ProcessedEvent.reconstitute(EVENT_ID, null));
  }

  @Test
  void equals_shouldReturnTrue_whenSameInstance() {
    ProcessedEvent event = ProcessedEvent.reconstitute(EVENT_ID, PROCESSED_AT);

    assertEquals(event, event);
  }

  @Test
  void equals_shouldReturnTrue_whenIdsAreEqual() {
    ProcessedEvent first = ProcessedEvent.reconstitute(EVENT_ID, PROCESSED_AT);

    ProcessedEvent second =
        ProcessedEvent.reconstitute(EVENT_ID, Instant.parse("2026-10-17T13:00:00Z"));

    assertEquals(first, second);
  }

  @Test
  void equals_shouldReturnFalse_whenIdsAreDifferent() {
    ProcessedEvent first = ProcessedEvent.reconstitute(EVENT_ID, PROCESSED_AT);

    ProcessedEvent second = ProcessedEvent.reconstitute(ANOTHER_EVENT_ID, PROCESSED_AT);

    assertNotEquals(first, second);
  }

  @Test
  void equals_shouldReturnFalse_whenComparedWithNull() {
    ProcessedEvent event = ProcessedEvent.reconstitute(EVENT_ID, PROCESSED_AT);

    assertNotEquals(null, event);
  }

  @Test
  void equals_shouldReturnFalse_whenComparedWithDifferentType() {
    ProcessedEvent event = ProcessedEvent.reconstitute(EVENT_ID, PROCESSED_AT);

    assertThat(event.equals("event")).isFalse();
  }

  @Test
  void hashCode_shouldBeEqual_whenIdsAreEqual() {
    ProcessedEvent first = ProcessedEvent.reconstitute(EVENT_ID, PROCESSED_AT);

    ProcessedEvent second = ProcessedEvent.reconstitute(EVENT_ID, PROCESSED_AT);

    assertEquals(first.hashCode(), second.hashCode());
  }

  @Test
  void hashCode_shouldBeDifferent_whenIdsAreDifferent() {
    ProcessedEvent first = ProcessedEvent.reconstitute(EVENT_ID, PROCESSED_AT);

    ProcessedEvent second = ProcessedEvent.reconstitute(ANOTHER_EVENT_ID, PROCESSED_AT);

    assertNotEquals(first.hashCode(), second.hashCode());
  }
}
