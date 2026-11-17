package com.jeannimi.messenger.application.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import com.jeannimi.messenger.application.notification.dto.UserNotificationSettingsResult;
import com.jeannimi.messenger.application.port.out.repository.UserNotificationSettingsRepositoryPort;
import com.jeannimi.messenger.domain.notification.UserNotificationSettings;
import com.jeannimi.messenger.domain.user.UserId;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserNotificationSettingsServiceTest {

  @Mock private UserNotificationSettingsRepositoryPort settingsRepository;

  @InjectMocks private UserNotificationSettingsService service;

  private UserId userId;

  @BeforeEach
  void setUp() {
    userId = new UserId(UUID.fromString("018f0000-0000-7000-8000-000000000001"));
  }

  // ---------------------------------------------------------------------------
  // getSettings
  // ---------------------------------------------------------------------------

  @Test
  void getSettings_shouldReturnExistingSettings() {
    UserNotificationSettings settings = createSettings(true, false, true, false);

    when(settingsRepository.findByUserId(userId)).thenReturn(Optional.of(settings));

    UserNotificationSettingsResult result = service.getSettings(userId);

    assertResultMatches(result, settings);

    verify(settingsRepository).findByUserId(userId);
    verify(settingsRepository, never()).save(any());
    verifyNoMoreInteractions(settingsRepository);
  }

  @Test
  void getSettings_shouldCreateDefaultSettingsWhenNotFound() {
    when(settingsRepository.findByUserId(userId)).thenReturn(Optional.empty());

    when(settingsRepository.save(any(UserNotificationSettings.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    UserNotificationSettingsResult result = service.getSettings(userId);

    ArgumentCaptor<UserNotificationSettings> captor =
        ArgumentCaptor.forClass(UserNotificationSettings.class);

    verify(settingsRepository).save(captor.capture());

    UserNotificationSettings saved = captor.getValue();

    assertThat(saved.getUserId()).isEqualTo(userId);

    assertThat(saved.isWebPushPrivateChatsEnabled()).isFalse();
    assertThat(saved.isWebPushGroupChatsEnabled()).isFalse();
    assertThat(saved.isEmailPrivateChatsEnabled()).isFalse();
    assertThat(saved.isEmailGroupChatsEnabled()).isFalse();

    assertThat(saved.getCreatedAt()).isNotNull();
    assertThat(saved.getUpdatedAt()).isEqualTo(saved.getCreatedAt());

    assertResultMatches(result, saved);

    verify(settingsRepository).findByUserId(userId);
    verifyNoMoreInteractions(settingsRepository);
  }

  @Test
  void getSettings_shouldThrowExceptionWhenUserIdIsNull() {
    assertThrows(NullPointerException.class, () -> service.getSettings(null));

    verifyNoInteractions(settingsRepository);
  }

  // ---------------------------------------------------------------------------
  // updateSettings
  // ---------------------------------------------------------------------------

  @Test
  void updateSettings_shouldUpdateAllSettings() {
    UserNotificationSettings settings = createSettings(false, false, false, false);

    when(settingsRepository.findByUserId(userId)).thenReturn(Optional.of(settings));

    when(settingsRepository.save(any(UserNotificationSettings.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    UserNotificationSettingsResult result =
        service.updateSettings(userId, true, false, true, false);

    ArgumentCaptor<UserNotificationSettings> captor =
        ArgumentCaptor.forClass(UserNotificationSettings.class);

    verify(settingsRepository).save(captor.capture());

    UserNotificationSettings saved = captor.getValue();

    assertThat(saved.isWebPushPrivateChatsEnabled()).isTrue();
    assertThat(saved.isWebPushGroupChatsEnabled()).isFalse();
    assertThat(saved.isEmailPrivateChatsEnabled()).isTrue();
    assertThat(saved.isEmailGroupChatsEnabled()).isFalse();

    assertThat(saved.getUpdatedAt()).isAfter(Instant.parse("2020-01-01T00:00:00Z"));

    assertResultMatches(result, saved);

    verify(settingsRepository).findByUserId(userId);
    verifyNoMoreInteractions(settingsRepository);
  }

  @Test
  void updateSettings_shouldUpdateOnlyProvidedSettings() {
    UserNotificationSettings settings = createSettings(true, true, true, true);

    when(settingsRepository.findByUserId(userId)).thenReturn(Optional.of(settings));

    when(settingsRepository.save(any(UserNotificationSettings.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    UserNotificationSettingsResult result = service.updateSettings(userId, false, null, null, null);

    ArgumentCaptor<UserNotificationSettings> captor =
        ArgumentCaptor.forClass(UserNotificationSettings.class);

    verify(settingsRepository).save(captor.capture());

    UserNotificationSettings saved = captor.getValue();

    assertThat(saved.isWebPushPrivateChatsEnabled()).isFalse();
    assertThat(saved.isWebPushGroupChatsEnabled()).isTrue();
    assertThat(saved.isEmailPrivateChatsEnabled()).isTrue();
    assertThat(saved.isEmailGroupChatsEnabled()).isTrue();

    assertResultMatches(result, saved);

    verify(settingsRepository).findByUserId(userId);
    verifyNoMoreInteractions(settingsRepository);
  }

  @Test
  void updateSettings_shouldCreateSettingsWhenNotFound() {
    when(settingsRepository.findByUserId(userId)).thenReturn(Optional.empty());

    when(settingsRepository.save(any(UserNotificationSettings.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    UserNotificationSettingsResult result =
        service.updateSettings(userId, true, false, true, false);

    ArgumentCaptor<UserNotificationSettings> captor =
        ArgumentCaptor.forClass(UserNotificationSettings.class);

    verify(settingsRepository).save(captor.capture());

    UserNotificationSettings saved = captor.getValue();

    assertThat(saved.isWebPushPrivateChatsEnabled()).isTrue();
    assertThat(saved.isWebPushGroupChatsEnabled()).isFalse();
    assertThat(saved.isEmailPrivateChatsEnabled()).isTrue();
    assertThat(saved.isEmailGroupChatsEnabled()).isFalse();

    assertResultMatches(result, saved);

    verify(settingsRepository).findByUserId(userId);
    verify(settingsRepository, times(1)).save(any());
    verifyNoMoreInteractions(settingsRepository);
  }

  @Test
  void updateSettings_shouldPreserveUpdatedAtWhenValuesAreUnchanged() {
    Instant timestamp = Instant.parse("2025-01-01T10:00:00Z");

    UserNotificationSettings settings =
        UserNotificationSettings.reconstitute(
            userId, true, false, true, false, timestamp, timestamp);

    when(settingsRepository.findByUserId(userId)).thenReturn(Optional.of(settings));

    when(settingsRepository.save(any(UserNotificationSettings.class)))
        .thenAnswer(invocation -> invocation.getArgument(0));

    UserNotificationSettingsResult result = service.updateSettings(userId, true, null, true, null);

    assertThat(result.updatedAt()).isEqualTo(timestamp);
    assertThat(result.createdAt()).isEqualTo(timestamp);

    verify(settingsRepository).findByUserId(userId);
    verify(settingsRepository).save(settings);
    verifyNoMoreInteractions(settingsRepository);
  }

  @Test
  void updateSettings_shouldThrowExceptionWhenAllSettingsAreNull() {
    assertThrows(
        IllegalArgumentException.class,
        () -> service.updateSettings(userId, null, null, null, null));

    verifyNoInteractions(settingsRepository);
  }

  @Test
  void updateSettings_shouldThrowExceptionWhenUserIdIsNull() {
    assertThrows(
        NullPointerException.class, () -> service.updateSettings(null, true, false, true, false));

    verifyNoInteractions(settingsRepository);
  }

  // ---------------------------------------------------------------------------
  // Test helpers
  // ---------------------------------------------------------------------------

  private UserNotificationSettings createSettings(
      boolean webPushPrivate, boolean webPushGroup, boolean emailPrivate, boolean emailGroup) {

    Instant timestamp = Instant.parse("2025-01-01T10:00:00Z");

    return UserNotificationSettings.reconstitute(
        userId, webPushPrivate, webPushGroup, emailPrivate, emailGroup, timestamp, timestamp);
  }

  private void assertResultMatches(
      UserNotificationSettingsResult actual, UserNotificationSettings expected) {

    assertThat(actual)
        .isEqualTo(
            new UserNotificationSettingsResult(
                expected.isWebPushPrivateChatsEnabled(),
                expected.isWebPushGroupChatsEnabled(),
                expected.isEmailPrivateChatsEnabled(),
                expected.isEmailGroupChatsEnabled(),
                expected.getCreatedAt(),
                expected.getUpdatedAt()));
  }
}
