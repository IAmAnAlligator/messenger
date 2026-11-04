package com.jeannimi.messenger.domain.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatIllegalArgumentException;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import com.jeannimi.messenger.domain.user.UserId;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class WebPushSubscriptionTest {

  private static final WebPushSubscriptionId SUBSCRIPTION_ID =
      new WebPushSubscriptionId(UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789012"));

  private static final WebPushSubscriptionId ANOTHER_SUBSCRIPTION_ID =
      new WebPushSubscriptionId(UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789013"));

  private static final UserId USER_ID =
      new UserId(UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789014"));

  private static final UserId ANOTHER_USER_ID =
      new UserId(UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789015"));

  private static final String ENDPOINT = "https://push.example.com/subscription/123";

  private static final String P256DH = "valid-p256dh-key";
  private static final String AUTH = "valid-auth-key";

  private static final Instant CREATED_AT = Instant.parse("2026-01-01T10:00:00Z");

  private static final Instant LAST_USED_AT = Instant.parse("2026-01-02T10:00:00Z");

  @Test
  void create_shouldCreateSubscription_whenValuesAreValid() {
    WebPushSubscription subscription =
        WebPushSubscription.create(SUBSCRIPTION_ID, USER_ID, ENDPOINT, P256DH, AUTH);

    assertThat(subscription.getId()).isEqualTo(SUBSCRIPTION_ID);
    assertThat(subscription.getUserId()).isEqualTo(USER_ID);
    assertThat(subscription.getEndpoint()).isEqualTo(ENDPOINT);
    assertThat(subscription.getP256dh()).isEqualTo(P256DH);
    assertThat(subscription.getAuth()).isEqualTo(AUTH);

    assertThat(subscription.getCreatedAt()).isNotNull();
    assertThat(subscription.getLastUsedAt()).isNull();
  }

  @Test
  void create_shouldThrowNullPointerException_whenIdIsNull() {
    assertThatNullPointerException()
        .isThrownBy(() -> WebPushSubscription.create(null, USER_ID, ENDPOINT, P256DH, AUTH))
        .withMessage("id");
  }

  @Test
  void create_shouldThrowNullPointerException_whenUserIdIsNull() {
    assertThatNullPointerException()
        .isThrownBy(() -> WebPushSubscription.create(SUBSCRIPTION_ID, null, ENDPOINT, P256DH, AUTH))
        .withMessage("userId");
  }

  @Test
  void create_shouldThrowNullPointerException_whenEndpointIsNull() {
    assertThatNullPointerException()
        .isThrownBy(() -> WebPushSubscription.create(SUBSCRIPTION_ID, USER_ID, null, P256DH, AUTH))
        .withMessage("endpoint");
  }

  @Test
  void create_shouldThrowNullPointerException_whenP256dhIsNull() {
    assertThatNullPointerException()
        .isThrownBy(
            () -> WebPushSubscription.create(SUBSCRIPTION_ID, USER_ID, ENDPOINT, null, AUTH))
        .withMessage("p256dh");
  }

  @Test
  void create_shouldThrowNullPointerException_whenAuthIsNull() {
    assertThatNullPointerException()
        .isThrownBy(
            () -> WebPushSubscription.create(SUBSCRIPTION_ID, USER_ID, ENDPOINT, P256DH, null))
        .withMessage("auth");
  }

  @Test
  void create_shouldThrowIllegalArgumentException_whenEndpointIsBlank() {
    assertThatIllegalArgumentException()
        .isThrownBy(() -> WebPushSubscription.create(SUBSCRIPTION_ID, USER_ID, "   ", P256DH, AUTH))
        .withMessage("endpoint must not be blank");
  }

  @Test
  void create_shouldThrowIllegalArgumentException_whenP256dhIsBlank() {
    assertThatIllegalArgumentException()
        .isThrownBy(
            () -> WebPushSubscription.create(SUBSCRIPTION_ID, USER_ID, ENDPOINT, "   ", AUTH))
        .withMessage("p256dh must not be blank");
  }

  @Test
  void create_shouldThrowIllegalArgumentException_whenAuthIsBlank() {
    assertThatIllegalArgumentException()
        .isThrownBy(
            () -> WebPushSubscription.create(SUBSCRIPTION_ID, USER_ID, ENDPOINT, P256DH, "   "))
        .withMessage("auth must not be blank");
  }

  @Test
  void reconstitute_shouldRestoreSubscription_whenValuesAreValid() {
    WebPushSubscription subscription =
        WebPushSubscription.reconstitute(
            SUBSCRIPTION_ID, USER_ID, ENDPOINT, P256DH, AUTH, CREATED_AT, LAST_USED_AT);

    assertThat(subscription.getId()).isEqualTo(SUBSCRIPTION_ID);
    assertThat(subscription.getUserId()).isEqualTo(USER_ID);
    assertThat(subscription.getEndpoint()).isEqualTo(ENDPOINT);
    assertThat(subscription.getP256dh()).isEqualTo(P256DH);
    assertThat(subscription.getAuth()).isEqualTo(AUTH);

    assertThat(subscription.getCreatedAt()).isEqualTo(CREATED_AT);
    assertThat(subscription.getLastUsedAt()).isEqualTo(LAST_USED_AT);
  }

  @Test
  void reconstitute_shouldAllowNullLastUsedAt_whenSubscriptionWasNeverUsed() {
    WebPushSubscription subscription =
        WebPushSubscription.reconstitute(
            SUBSCRIPTION_ID, USER_ID, ENDPOINT, P256DH, AUTH, CREATED_AT, null);

    assertThat(subscription.getLastUsedAt()).isNull();
  }

  @Test
  void reconstitute_shouldThrowNullPointerException_whenCreatedAtIsNull() {
    assertThatNullPointerException()
        .isThrownBy(
            () ->
                WebPushSubscription.reconstitute(
                    SUBSCRIPTION_ID, USER_ID, ENDPOINT, P256DH, AUTH, null, LAST_USED_AT))
        .withMessage("createdAt");
  }

  @Test
  void equals_shouldReturnTrue_whenIdsAreEqual() {
    WebPushSubscription first =
        WebPushSubscription.reconstitute(
            SUBSCRIPTION_ID, USER_ID, ENDPOINT, P256DH, AUTH, CREATED_AT, LAST_USED_AT);

    WebPushSubscription second =
        WebPushSubscription.reconstitute(
            SUBSCRIPTION_ID,
            ANOTHER_USER_ID,
            "https://another.example.com/subscription/456",
            "another-p256dh",
            "another-auth",
            LAST_USED_AT,
            CREATED_AT);

    assertThat(first).isEqualTo(second);
  }

  @Test
  void equals_shouldReturnFalse_whenIdsAreDifferent() {
    WebPushSubscription first =
        WebPushSubscription.create(SUBSCRIPTION_ID, USER_ID, ENDPOINT, P256DH, AUTH);

    WebPushSubscription second =
        WebPushSubscription.create(ANOTHER_SUBSCRIPTION_ID, USER_ID, ENDPOINT, P256DH, AUTH);

    assertThat(first).isNotEqualTo(second);
  }

  @Test
  void equals_shouldReturnTrue_whenComparedWithItself() {
    WebPushSubscription subscription =
        WebPushSubscription.create(SUBSCRIPTION_ID, USER_ID, ENDPOINT, P256DH, AUTH);

    assertThat(subscription).isEqualTo(subscription);
  }

  @Test
  void equals_shouldReturnFalse_whenComparedWithNull() {
    WebPushSubscription subscription =
        WebPushSubscription.create(SUBSCRIPTION_ID, USER_ID, ENDPOINT, P256DH, AUTH);

    assertThat(subscription).isNotEqualTo(null);
  }

  @Test
  void equals_shouldReturnFalse_whenComparedWithDifferentType() {
    WebPushSubscription subscription =
        WebPushSubscription.create(SUBSCRIPTION_ID, USER_ID, ENDPOINT, P256DH, AUTH);

    assertThat(subscription.equals("subscription")).isFalse();
  }

  @Test
  void hashCode_shouldBeEqual_whenIdsAreEqual() {
    WebPushSubscription first =
        WebPushSubscription.create(SUBSCRIPTION_ID, USER_ID, ENDPOINT, P256DH, AUTH);

    WebPushSubscription second =
        WebPushSubscription.create(SUBSCRIPTION_ID, ANOTHER_USER_ID, ENDPOINT, P256DH, AUTH);

    assertThat(first.hashCode()).isEqualTo(second.hashCode());
  }

  @Test
  void markAsUsed_shouldUpdateLastUsedAt_whenUsedAtIsValid() {
    WebPushSubscription subscription =
        WebPushSubscription.reconstitute(
            SUBSCRIPTION_ID, USER_ID, ENDPOINT, P256DH, AUTH, CREATED_AT, null);

    subscription.markAsUsed(LAST_USED_AT);

    assertThat(subscription.getLastUsedAt()).isEqualTo(LAST_USED_AT);
  }

  @Test
  void markAsUsed_shouldReplaceLastUsedAt_whenAlreadySet() {
    Instant anotherUsedAt = Instant.parse("2026-01-03T10:00:00Z");

    WebPushSubscription subscription =
        WebPushSubscription.reconstitute(
            SUBSCRIPTION_ID, USER_ID, ENDPOINT, P256DH, AUTH, CREATED_AT, LAST_USED_AT);

    subscription.markAsUsed(anotherUsedAt);

    assertThat(subscription.getLastUsedAt()).isEqualTo(anotherUsedAt);
  }

  @Test
  void markAsUsed_shouldThrowNullPointerException_whenUsedAtIsNull() {
    WebPushSubscription subscription =
        WebPushSubscription.create(SUBSCRIPTION_ID, USER_ID, ENDPOINT, P256DH, AUTH);

    assertThatNullPointerException()
        .isThrownBy(() -> subscription.markAsUsed(null))
        .withMessage("usedAt");
  }
}
