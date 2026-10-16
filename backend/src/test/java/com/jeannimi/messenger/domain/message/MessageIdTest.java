package com.jeannimi.messenger.domain.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class MessageIdTest {

  private static final UUID UUID_VALUE = UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789012");

  private static final UUID ANOTHER_UUID_VALUE =
      UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789013");

  @Test
  void constructor_shouldCreateId_whenValueIsNotNull() {
    MessageId id = new MessageId(UUID_VALUE);

    assertNotNull(id);
    assertEquals(UUID_VALUE, id.value());
  }

  @Test
  void constructor_shouldThrowNullPointerException_whenValueIsNull() {
    NullPointerException exception =
        assertThrows(NullPointerException.class, () -> new MessageId(null));

    assertEquals("Message id must not be null", exception.getMessage());
  }

  @Test
  void equals_shouldReturnTrue_whenIdsHaveEqualValues() {
    MessageId first = new MessageId(UUID_VALUE);
    MessageId second = new MessageId(UUID_VALUE);

    assertEquals(first, second);
  }

  @Test
  void equals_shouldReturnFalse_whenIdsHaveDifferentValues() {
    MessageId first = new MessageId(UUID_VALUE);
    MessageId second = new MessageId(ANOTHER_UUID_VALUE);

    assertNotEquals(first, second);
  }

  @Test
  void hashCode_shouldBeEqual_whenIdsHaveEqualValues() {
    MessageId first = new MessageId(UUID_VALUE);
    MessageId second = new MessageId(UUID_VALUE);

    assertEquals(first.hashCode(), second.hashCode());
  }

  @Test
  void hashCode_shouldBeDifferent_whenIdsHaveDifferentValues() {
    MessageId first = new MessageId(UUID_VALUE);
    MessageId second = new MessageId(ANOTHER_UUID_VALUE);

    assertNotEquals(first.hashCode(), second.hashCode());
  }
}
