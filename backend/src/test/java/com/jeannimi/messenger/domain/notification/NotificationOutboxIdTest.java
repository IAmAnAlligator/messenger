package com.jeannimi.messenger.domain.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class NotificationOutboxIdTest {

  private static final UUID OUTBOX_UUID = UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789012");

  private static final UUID ANOTHER_OUTBOX_UUID =
      UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789013");

  @Test
  void constructor_shouldCreateNotificationOutboxId_whenValueIsValid() {
    NotificationOutboxId outboxId = new NotificationOutboxId(OUTBOX_UUID);

    assertThat(outboxId.value()).isEqualTo(OUTBOX_UUID);
  }

  @Test
  void constructor_shouldThrowNullPointerException_whenValueIsNull() {
    assertThatNullPointerException()
        .isThrownBy(() -> new NotificationOutboxId(null))
        .withMessage("value");
  }

  @Test
  void equals_shouldReturnTrue_whenValuesAreEqual() {
    NotificationOutboxId first = new NotificationOutboxId(OUTBOX_UUID);
    NotificationOutboxId second = new NotificationOutboxId(OUTBOX_UUID);

    assertThat(first).isEqualTo(second);
  }

  @Test
  void equals_shouldReturnFalse_whenValuesAreDifferent() {
    NotificationOutboxId first = new NotificationOutboxId(OUTBOX_UUID);
    NotificationOutboxId second = new NotificationOutboxId(ANOTHER_OUTBOX_UUID);

    assertThat(first).isNotEqualTo(second);
  }

  @Test
  void equals_shouldReturnTrue_whenComparedWithItself() {
    NotificationOutboxId outboxId = new NotificationOutboxId(OUTBOX_UUID);

    assertThat(outboxId).isEqualTo(outboxId);
  }

  @Test
  void equals_shouldReturnFalse_whenComparedWithNull() {
    NotificationOutboxId outboxId = new NotificationOutboxId(OUTBOX_UUID);

    assertThat(outboxId).isNotEqualTo(null);
  }

  @Test
  void equals_shouldReturnFalse_whenComparedWithDifferentType() {
    NotificationOutboxId outboxId = new NotificationOutboxId(OUTBOX_UUID);

    assertThat(outboxId).isNotEqualTo(OUTBOX_UUID);
  }

  @Test
  void hashCode_shouldBeEqual_whenValuesAreEqual() {
    NotificationOutboxId first = new NotificationOutboxId(OUTBOX_UUID);
    NotificationOutboxId second = new NotificationOutboxId(OUTBOX_UUID);

    assertThat(first.hashCode()).isEqualTo(second.hashCode());
  }
}
