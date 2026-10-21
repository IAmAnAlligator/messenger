package com.jeannimi.messenger.adapter.in.web.notification;

import com.jeannimi.messenger.adapter.in.security.CustomUserDetails;
import com.jeannimi.messenger.adapter.in.web.notification.dto.UpdateNotificationSettingsRequest;
import com.jeannimi.messenger.adapter.in.web.notification.dto.UserNotificationSettingsResponse;
import com.jeannimi.messenger.application.notification.dto.UserNotificationSettingsResult;
import com.jeannimi.messenger.application.port.in.UserNotificationSettingsUseCase;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications/settings")
@RequiredArgsConstructor
public class UserNotificationSettingsController {

  private final UserNotificationSettingsUseCase settingsUseCase;

  @GetMapping
  public ResponseEntity<UserNotificationSettingsResponse> getSettings(
      @AuthenticationPrincipal CustomUserDetails user) {

    UserNotificationSettingsResult result = settingsUseCase.getSettings(user.id());

    return ResponseEntity.ok(toResponse(result));
  }

  @PatchMapping
  public ResponseEntity<UserNotificationSettingsResponse> updateSettings(
      @AuthenticationPrincipal CustomUserDetails user,
      @Valid @RequestBody UpdateNotificationSettingsRequest request) {

    UserNotificationSettingsResult result =
        settingsUseCase.updateWebPushEnabled(user.id(), request.webPushEnabled());

    return ResponseEntity.ok(toResponse(result));
  }

  private UserNotificationSettingsResponse toResponse(UserNotificationSettingsResult result) {

    return new UserNotificationSettingsResponse(
        result.webPushEnabled(), result.createdAt(), result.updatedAt());
  }
}
