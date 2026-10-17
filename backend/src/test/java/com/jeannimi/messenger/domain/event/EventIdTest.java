package com.jeannimi.messenger.domain.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class EventIdTest {

  private static final UUID UUID_VALUE = UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789012");

  private static final UUID ANOTHER_UUID_VALUE =
      UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789013");

  @Test
  void constructor_shouldCreateEventId_whenValueIsValid() {
    EventId eventId = new EventId(UUID_VALUE);

    assertEquals(UUID_VALUE, eventId.value());
  }

  @Test
  void constructor_shouldThrowNullPointerException_whenValueIsNull() {
    NullPointerException exception =
        assertThrows(NullPointerException.class, () -> new EventId(null));

    assertEquals("Event id must not be null", exception.getMessage());
  }

  @Test
  void equals_shouldReturnTrue_whenIdsHaveSameValue() {
    EventId first = new EventId(UUID_VALUE);
    EventId second = new EventId(UUID_VALUE);

    assertEquals(first, second);
  }

  @Test
  void equals_shouldReturnFalse_whenIdsHaveDifferentValues() {
    EventId first = new EventId(UUID_VALUE);
    EventId second = new EventId(ANOTHER_UUID_VALUE);

    assertNotEquals(first, second);
  }

  @Test
  void equals_shouldReturnTrue_whenComparingSameInstance() {
    EventId eventId = new EventId(UUID_VALUE);

    assertEquals(eventId, eventId);
  }

  @Test
  void equals_shouldReturnFalse_whenComparedWithNull() {
    EventId eventId = new EventId(UUID_VALUE);

    assertNotEquals(null, eventId);
  }

  @Test
  void equals_shouldReturnFalse_whenComparedWithDifferentType() {
    EventId eventId = new EventId(UUID_VALUE);

    assertEquals(false, eventId.equals("event"));
  }

  @Test
  void hashCode_shouldBeEqual_whenIdsHaveSameValue() {
    EventId first = new EventId(UUID_VALUE);
    EventId second = new EventId(UUID_VALUE);

    assertEquals(first.hashCode(), second.hashCode());
  }

  @Test
  void hashCode_shouldBeDifferent_whenIdsHaveDifferentValues() {
    EventId first = new EventId(UUID_VALUE);
    EventId second = new EventId(ANOTHER_UUID_VALUE);

    assertNotEquals(first.hashCode(), second.hashCode());
  }

  @Test
  void toString_shouldContainValue() {
    EventId eventId = new EventId(UUID_VALUE);

    assertEquals("EventId[value=" + UUID_VALUE + "]", eventId.toString());
  }
}
