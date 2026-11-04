package com.jeannimi.messenger.domain.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import java.util.UUID;
import org.junit.jupiter.api.Test;

class WebPushSubscriptionIdTest {

  private static final UUID SUBSCRIPTION_UUID =
      UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789012");

  private static final UUID ANOTHER_SUBSCRIPTION_UUID =
      UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789013");

  @Test
  void constructor_shouldCreateWebPushSubscriptionId_whenValueIsValid() {
    WebPushSubscriptionId subscriptionId = new WebPushSubscriptionId(SUBSCRIPTION_UUID);

    assertThat(subscriptionId.value()).isEqualTo(SUBSCRIPTION_UUID);
  }

  @Test
  void constructor_shouldThrowNullPointerException_whenValueIsNull() {
    assertThatNullPointerException()
        .isThrownBy(() -> new WebPushSubscriptionId(null))
        .withMessage("Web push subscription id must not be null");
  }

  @Test
  void equals_shouldReturnTrue_whenValuesAreEqual() {
    WebPushSubscriptionId first = new WebPushSubscriptionId(SUBSCRIPTION_UUID);
    WebPushSubscriptionId second = new WebPushSubscriptionId(SUBSCRIPTION_UUID);

    assertThat(first).isEqualTo(second);
  }

  @Test
  void equals_shouldReturnFalse_whenValuesAreDifferent() {
    WebPushSubscriptionId first = new WebPushSubscriptionId(SUBSCRIPTION_UUID);
    WebPushSubscriptionId second = new WebPushSubscriptionId(ANOTHER_SUBSCRIPTION_UUID);

    assertThat(first).isNotEqualTo(second);
  }

  @Test
  void equals_shouldReturnTrue_whenComparedWithItself() {
    WebPushSubscriptionId subscriptionId = new WebPushSubscriptionId(SUBSCRIPTION_UUID);

    assertThat(subscriptionId).isEqualTo(subscriptionId);
  }

  @Test
  void equals_shouldReturnFalse_whenComparedWithNull() {
    WebPushSubscriptionId subscriptionId = new WebPushSubscriptionId(SUBSCRIPTION_UUID);

    assertThat(subscriptionId).isNotEqualTo(null);
  }

  @Test
  void equals_shouldReturnFalse_whenComparedWithDifferentType() {
    WebPushSubscriptionId subscriptionId = new WebPushSubscriptionId(SUBSCRIPTION_UUID);

    assertThat(subscriptionId).isNotEqualTo(SUBSCRIPTION_UUID);
  }

  @Test
  void hashCode_shouldBeEqual_whenValuesAreEqual() {
    WebPushSubscriptionId first = new WebPushSubscriptionId(SUBSCRIPTION_UUID);
    WebPushSubscriptionId second = new WebPushSubscriptionId(SUBSCRIPTION_UUID);

    assertThat(first.hashCode()).isEqualTo(second.hashCode());
  }
}
