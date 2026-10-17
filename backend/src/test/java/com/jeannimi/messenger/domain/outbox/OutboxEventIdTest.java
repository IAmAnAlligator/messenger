package com.jeannimi.messenger.domain.outbox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class OutboxEventIdTest {

  private static final UUID UUID_VALUE = UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789012");

  private static final UUID ANOTHER_UUID_VALUE =
      UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789013");

  @Test
  void constructor_shouldCreateOutboxEventId_whenValueIsValid() {
    OutboxEventId outboxEventId = new OutboxEventId(UUID_VALUE);

    assertEquals(UUID_VALUE, outboxEventId.value());
  }

  @Test
  void constructor_shouldThrowNullPointerException_whenValueIsNull() {
    NullPointerException exception =
        assertThrows(NullPointerException.class, () -> new OutboxEventId(null));

    assertEquals("OutboxEvent id must not be null", exception.getMessage());
  }

  @Test
  void equals_shouldReturnTrue_whenValuesAreEqual() {
    OutboxEventId first = new OutboxEventId(UUID_VALUE);
    OutboxEventId second = new OutboxEventId(UUID_VALUE);

    assertEquals(first, second);
  }

  @Test
  void equals_shouldReturnFalse_whenValuesAreDifferent() {
    OutboxEventId first = new OutboxEventId(UUID_VALUE);
    OutboxEventId second = new OutboxEventId(ANOTHER_UUID_VALUE);

    assertNotEquals(first, second);
  }

  @Test
  void equals_shouldReturnTrue_whenComparingSameInstance() {
    OutboxEventId outboxEventId = new OutboxEventId(UUID_VALUE);

    assertEquals(outboxEventId, outboxEventId);
  }

  @Test
  void equals_shouldReturnFalse_whenComparedWithNull() {
    OutboxEventId outboxEventId = new OutboxEventId(UUID_VALUE);

    assertNotEquals(null, outboxEventId);
  }

  @Test
  void equals_shouldReturnFalse_whenComparedWithDifferentType() {
    OutboxEventId outboxEventId = new OutboxEventId(UUID_VALUE);

    assertFalse(outboxEventId.equals("outbox-event"));
  }

  @Test
  void hashCode_shouldBeEqual_whenValuesAreEqual() {
    OutboxEventId first = new OutboxEventId(UUID_VALUE);
    OutboxEventId second = new OutboxEventId(UUID_VALUE);

    assertEquals(first.hashCode(), second.hashCode());
  }

  @Test
  void hashCode_shouldBeDifferent_whenValuesAreDifferent() {
    OutboxEventId first = new OutboxEventId(UUID_VALUE);
    OutboxEventId second = new OutboxEventId(ANOTHER_UUID_VALUE);

    assertNotEquals(first.hashCode(), second.hashCode());
  }

  @Test
  void toString_shouldReturnExpectedRepresentation() {
    OutboxEventId outboxEventId = new OutboxEventId(UUID_VALUE);

    assertEquals("OutboxEventId[value=" + UUID_VALUE + "]", outboxEventId.toString());
  }
}
