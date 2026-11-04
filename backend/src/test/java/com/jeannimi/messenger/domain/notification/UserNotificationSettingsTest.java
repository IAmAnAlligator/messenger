package com.jeannimi.messenger.domain.notification;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNullPointerException;

import com.jeannimi.messenger.domain.user.UserId;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class UserNotificationSettingsTest {

  private static final UserId USER_ID =
      new UserId(UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789012"));

  private static final UserId ANOTHER_USER_ID =
      new UserId(UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789013"));

  private static final Instant CREATED_AT = Instant.parse("2026-01-01T10:00:00Z");

  private static final Instant UPDATED_AT = Instant.parse("2026-01-02T10:00:00Z");

  @Test
  void create_shouldCreateSettingsWithDisabledNotifications_whenUserIdIsValid() {
    UserNotificationSettings settings = UserNotificationSettings.create(USER_ID);

    assertThat(settings.getUserId()).isEqualTo(USER_ID);

    assertThat(settings.isWebPushPrivateChatsEnabled()).isFalse();
    assertThat(settings.isWebPushGroupChatsEnabled()).isFalse();

    assertThat(settings.isEmailPrivateChatsEnabled()).isFalse();
    assertThat(settings.isEmailGroupChatsEnabled()).isFalse();

    assertThat(settings.getCreatedAt()).isNotNull();
    assertThat(settings.getUpdatedAt()).isEqualTo(settings.getCreatedAt());
  }

  @Test
  void create_shouldThrowNullPointerException_whenUserIdIsNull() {
    assertThatNullPointerException()
        .isThrownBy(() -> UserNotificationSettings.create(null))
        .withMessage("userId");
  }

  @Test
  void reconstitute_shouldRestoreSettings_whenValuesAreValid() {
    UserNotificationSettings settings =
        UserNotificationSettings.reconstitute(
            USER_ID, true, false, false, true, CREATED_AT, UPDATED_AT);

    assertThat(settings.getUserId()).isEqualTo(USER_ID);

    assertThat(settings.isWebPushPrivateChatsEnabled()).isTrue();
    assertThat(settings.isWebPushGroupChatsEnabled()).isFalse();

    assertThat(settings.isEmailPrivateChatsEnabled()).isFalse();
    assertThat(settings.isEmailGroupChatsEnabled()).isTrue();

    assertThat(settings.getCreatedAt()).isEqualTo(CREATED_AT);
    assertThat(settings.getUpdatedAt()).isEqualTo(UPDATED_AT);
  }

  @Test
  void reconstitute_shouldThrowNullPointerException_whenUserIdIsNull() {
    assertThatNullPointerException()
        .isThrownBy(
            () ->
                UserNotificationSettings.reconstitute(
                    null, false, false, false, false, CREATED_AT, UPDATED_AT))
        .withMessage("userId");
  }

  @Test
  void reconstitute_shouldThrowNullPointerException_whenCreatedAtIsNull() {
    assertThatNullPointerException()
        .isThrownBy(
            () ->
                UserNotificationSettings.reconstitute(
                    USER_ID, false, false, false, false, null, UPDATED_AT))
        .withMessage("createdAt");
  }

  @Test
  void reconstitute_shouldThrowNullPointerException_whenUpdatedAtIsNull() {
    assertThatNullPointerException()
        .isThrownBy(
            () ->
                UserNotificationSettings.reconstitute(
                    USER_ID, false, false, false, false, CREATED_AT, null))
        .withMessage("updatedAt");
  }

  @Test
  void enableNotifications_shouldEnableAllChannels_whenValuesAreChanged() {
    UserNotificationSettings settings =
        UserNotificationSettings.reconstitute(
            USER_ID, false, false, false, false, CREATED_AT, UPDATED_AT);

    settings.enableWebPushPrivateChats(true);
    settings.enableWebPushGroupChats(true);
    settings.enableEmailPrivateChats(true);
    settings.enableEmailGroupChats(true);

    assertThat(settings.isWebPushPrivateChatsEnabled()).isTrue();
    assertThat(settings.isWebPushGroupChatsEnabled()).isTrue();

    assertThat(settings.isEmailPrivateChatsEnabled()).isTrue();
    assertThat(settings.isEmailGroupChatsEnabled()).isTrue();

    assertThat(settings.getUpdatedAt()).isAfter(UPDATED_AT);
  }

  @Test
  void disableNotifications_shouldDisableAllChannels_whenValuesAreChanged() {
    UserNotificationSettings settings =
        UserNotificationSettings.reconstitute(
            USER_ID, true, true, true, true, CREATED_AT, UPDATED_AT);

    settings.enableWebPushPrivateChats(false);
    settings.enableWebPushGroupChats(false);
    settings.enableEmailPrivateChats(false);
    settings.enableEmailGroupChats(false);

    assertThat(settings.isWebPushPrivateChatsEnabled()).isFalse();
    assertThat(settings.isWebPushGroupChatsEnabled()).isFalse();

    assertThat(settings.isEmailPrivateChatsEnabled()).isFalse();
    assertThat(settings.isEmailGroupChatsEnabled()).isFalse();

    assertThat(settings.getUpdatedAt()).isAfter(UPDATED_AT);
  }

  @Test
  void enableNotifications_shouldNotUpdateTimestamp_whenValuesAreUnchanged() {
    UserNotificationSettings settings =
        UserNotificationSettings.reconstitute(
            USER_ID, false, false, false, false, CREATED_AT, UPDATED_AT);

    settings.enableWebPushPrivateChats(false);
    settings.enableWebPushGroupChats(false);
    settings.enableEmailPrivateChats(false);
    settings.enableEmailGroupChats(false);

    assertThat(settings.getUpdatedAt()).isEqualTo(UPDATED_AT);
  }

  @Test
  void disableNotifications_shouldNotUpdateTimestamp_whenValuesAreUnchanged() {
    UserNotificationSettings settings =
        UserNotificationSettings.reconstitute(
            USER_ID, true, true, true, true, CREATED_AT, UPDATED_AT);

    settings.enableWebPushPrivateChats(true);
    settings.enableWebPushGroupChats(true);
    settings.enableEmailPrivateChats(true);
    settings.enableEmailGroupChats(true);

    assertThat(settings.getUpdatedAt()).isEqualTo(UPDATED_AT);
  }

  @Test
  void equals_shouldReturnTrue_whenUserIdsAreEqual() {
    UserNotificationSettings first =
        UserNotificationSettings.reconstitute(
            USER_ID, true, false, true, false, CREATED_AT, UPDATED_AT);

    UserNotificationSettings second =
        UserNotificationSettings.reconstitute(
            USER_ID, false, true, false, true, UPDATED_AT, CREATED_AT);

    assertThat(first).isEqualTo(second);
  }

  @Test
  void equals_shouldReturnFalse_whenUserIdsAreDifferent() {
    UserNotificationSettings first = UserNotificationSettings.create(USER_ID);

    UserNotificationSettings second = UserNotificationSettings.create(ANOTHER_USER_ID);

    assertThat(first).isNotEqualTo(second);
  }

  @Test
  void equals_shouldReturnTrue_whenComparedWithItself() {
    UserNotificationSettings settings = UserNotificationSettings.create(USER_ID);

    assertThat(settings).isEqualTo(settings);
  }

  @Test
  void equals_shouldReturnFalse_whenComparedWithNull() {
    UserNotificationSettings settings = UserNotificationSettings.create(USER_ID);

    assertThat(settings).isNotEqualTo(null);
  }

  @Test
  void equals_shouldReturnFalse_whenComparedWithDifferentType() {
    UserNotificationSettings settings = UserNotificationSettings.create(USER_ID);

    assertThat(settings.equals("settings")).isFalse();
  }

  @Test
  void hashCode_shouldBeEqual_whenUserIdsAreEqual() {
    UserNotificationSettings first = UserNotificationSettings.create(USER_ID);

    UserNotificationSettings second = UserNotificationSettings.create(USER_ID);

    assertThat(first.hashCode()).isEqualTo(second.hashCode());
  }
}
