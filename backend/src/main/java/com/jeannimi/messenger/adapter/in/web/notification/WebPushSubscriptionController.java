package com.jeannimi.messenger.adapter.in.web.notification;

import com.jeannimi.messenger.adapter.in.security.CustomUserDetails;
import com.jeannimi.messenger.adapter.in.web.notification.dto.WebPushSubscriptionRequest;
import com.jeannimi.messenger.adapter.in.web.notification.dto.WebPushSubscriptionResponse;
import com.jeannimi.messenger.application.notification.dto.WebPushSubscriptionResult;
import com.jeannimi.messenger.application.port.in.WebPushSubscriptionUseCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/notifications/push/subscriptions")
@RequiredArgsConstructor
@Validated
public class WebPushSubscriptionController {

  private final WebPushSubscriptionUseCase webPushSubscriptionUseCase;

  @PostMapping
  public ResponseEntity<WebPushSubscriptionResponse> register(
      @AuthenticationPrincipal CustomUserDetails user,
      @Valid @RequestBody WebPushSubscriptionRequest request) {

    WebPushSubscriptionResult result =
        webPushSubscriptionUseCase.register(
            user.id(), request.endpoint(), request.p256dh(), request.auth());

    return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(result));
  }

  @GetMapping
  public List<WebPushSubscriptionResponse> getSubscriptions(
      @AuthenticationPrincipal CustomUserDetails user) {

    return webPushSubscriptionUseCase.getSubscriptions(user.id()).stream()
        .map(this::toResponse)
        .toList();
  }

  @DeleteMapping
  public ResponseEntity<Void> delete(
      @AuthenticationPrincipal CustomUserDetails user, @RequestParam @NotBlank String endpoint) {
    webPushSubscriptionUseCase.delete(user.id(), endpoint);

    return ResponseEntity.noContent().build();
  }

  private WebPushSubscriptionResponse toResponse(WebPushSubscriptionResult result) {
    return new WebPushSubscriptionResponse(
        result.id(), result.endpoint(), result.createdAt(), result.lastUsedAt());
  }
}
