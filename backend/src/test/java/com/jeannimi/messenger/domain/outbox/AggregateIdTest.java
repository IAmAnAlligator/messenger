package com.jeannimi.messenger.domain.outbox;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class AggregateIdTest {

  private static final UUID UUID_VALUE = UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789012");

  private static final UUID ANOTHER_UUID_VALUE =
      UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789013");

  @Test
  void constructor_shouldCreateAggregateId_whenValueIsValid() {
    AggregateId aggregateId = new AggregateId(UUID_VALUE);

    assertEquals(UUID_VALUE, aggregateId.value());
  }

  @Test
  void constructor_shouldThrowNullPointerException_whenValueIsNull() {
    NullPointerException exception =
        assertThrows(NullPointerException.class, () -> new AggregateId(null));

    assertEquals("AggregateId id must not be null", exception.getMessage());
  }

  @Test
  void equals_shouldReturnTrue_whenValuesAreEqual() {
    AggregateId first = new AggregateId(UUID_VALUE);
    AggregateId second = new AggregateId(UUID_VALUE);

    assertEquals(first, second);
  }

  @Test
  void equals_shouldReturnFalse_whenValuesAreDifferent() {
    AggregateId first = new AggregateId(UUID_VALUE);
    AggregateId second = new AggregateId(ANOTHER_UUID_VALUE);

    assertNotEquals(first, second);
  }

  @Test
  void equals_shouldReturnTrue_whenComparingSameInstance() {
    AggregateId aggregateId = new AggregateId(UUID_VALUE);

    assertEquals(aggregateId, aggregateId);
  }

  @Test
  void equals_shouldReturnFalse_whenComparedWithNull() {
    AggregateId aggregateId = new AggregateId(UUID_VALUE);

    assertNotEquals(null, aggregateId);
  }

  @Test
  void equals_shouldReturnFalse_whenComparedWithDifferentType() {
    AggregateId aggregateId = new AggregateId(UUID_VALUE);

    assertFalse(aggregateId.equals("aggregate"));
  }

  @Test
  void hashCode_shouldBeEqual_whenValuesAreEqual() {
    AggregateId first = new AggregateId(UUID_VALUE);
    AggregateId second = new AggregateId(UUID_VALUE);

    assertEquals(first.hashCode(), second.hashCode());
  }

  @Test
  void hashCode_shouldBeDifferent_whenValuesAreDifferent() {
    AggregateId first = new AggregateId(UUID_VALUE);
    AggregateId second = new AggregateId(ANOTHER_UUID_VALUE);

    assertNotEquals(first.hashCode(), second.hashCode());
  }

  @Test
  void toString_shouldReturnExpectedRepresentation() {
    AggregateId aggregateId = new AggregateId(UUID_VALUE);

    assertEquals("AggregateId[value=" + UUID_VALUE + "]", aggregateId.toString());
  }
}
