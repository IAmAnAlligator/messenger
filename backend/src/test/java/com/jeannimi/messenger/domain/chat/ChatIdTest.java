package com.jeannimi.messenger.domain.chat;

import static org.junit.jupiter.api.Assertions.*;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class ChatIdTest {

  private static final UUID UUID_VALUE = UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789012");

  @Test
  void constructor_shouldCreateChatId_whenValueIsValid() {
    ChatId chatId = new ChatId(UUID_VALUE);

    assertEquals(UUID_VALUE, chatId.value());
  }

  @Test
  void constructor_shouldThrowNullPointerException_whenValueIsNull() {
    NullPointerException exception =
        assertThrows(NullPointerException.class, () -> new ChatId(null));

    assertEquals("Chat id must not be null", exception.getMessage());
  }

  @Test
  void equals_shouldReturnTrue_whenValuesAreEqual() {
    ChatId first = new ChatId(UUID_VALUE);
    ChatId second = new ChatId(UUID_VALUE);

    assertEquals(first, second);
  }

  @Test
  void equals_shouldReturnFalse_whenValuesAreDifferent() {
    ChatId first = new ChatId(UUID_VALUE);
    ChatId second = new ChatId(UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789013"));

    assertNotEquals(first, second);
  }

  @Test
  void hashCode_shouldBeEqual_whenValuesAreEqual() {
    ChatId first = new ChatId(UUID_VALUE);
    ChatId second = new ChatId(UUID_VALUE);

    assertEquals(first.hashCode(), second.hashCode());
  }
}
