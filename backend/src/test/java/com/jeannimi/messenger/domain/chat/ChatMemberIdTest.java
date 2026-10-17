package com.jeannimi.messenger.domain.chat;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class ChatMemberIdTest {

  private static final UUID UUID_VALUE = UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789012");

  @Test
  void constructor_shouldCreateChatMemberId_whenValueIsValid() {
    ChatMemberId chatMemberId = new ChatMemberId(UUID_VALUE);

    assertEquals(UUID_VALUE, chatMemberId.value());
  }

  @Test
  void constructor_shouldThrowNullPointerException_whenValueIsNull() {
    NullPointerException exception =
        assertThrows(NullPointerException.class, () -> new ChatMemberId(null));

    assertEquals("Chat member id must not be null", exception.getMessage());
  }

  @Test
  void equals_shouldReturnTrue_whenValuesAreEqual() {
    ChatMemberId first = new ChatMemberId(UUID_VALUE);
    ChatMemberId second = new ChatMemberId(UUID_VALUE);

    assertEquals(first, second);
  }

  @Test
  void equals_shouldReturnFalse_whenValuesAreDifferent() {
    ChatMemberId first = new ChatMemberId(UUID_VALUE);
    ChatMemberId second = new ChatMemberId(UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789013"));

    assertNotEquals(first, second);
  }

  @Test
  void equals_shouldReturnFalse_whenComparingNull() {
    ChatMemberId chatMemberId = new ChatMemberId(UUID_VALUE);

    assertNotEquals(null, chatMemberId);
  }

  @Test
  void equals_shouldReturnFalse_whenComparingDifferentType() {
    ChatMemberId chatMemberId = new ChatMemberId(UUID_VALUE);

    assertNotEquals(UUID_VALUE, chatMemberId);
  }

  @Test
  void hashCode_shouldBeEqual_whenValuesAreEqual() {
    ChatMemberId first = new ChatMemberId(UUID_VALUE);
    ChatMemberId second = new ChatMemberId(UUID_VALUE);

    assertEquals(first.hashCode(), second.hashCode());
  }

  @Test
  void hashCode_shouldBeEqualToValueHashCode() {
    ChatMemberId chatMemberId = new ChatMemberId(UUID_VALUE);

    assertEquals(UUID_VALUE.hashCode(), chatMemberId.hashCode());
  }
}
