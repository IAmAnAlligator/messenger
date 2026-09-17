package com.jeannimi.messenger.adapter.in.web.auth;

import com.jeannimi.messenger.application.auth.dto.AuthResult;
import com.jeannimi.messenger.adapter.in.web.auth.dto.AuthAccessResponse;
import com.jeannimi.messenger.adapter.in.web.auth.dto.LoginRequest;
import com.jeannimi.messenger.adapter.in.web.auth.dto.RegisterRequest;
import com.jeannimi.messenger.application.auth.service.AuthService;
import jakarta.validation.Valid;
import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private static final String REFRESH_COOKIE = "refreshToken";
  private static final String COOKIE_PATH = "/";
  private static final String SAME_SITE = "Lax";
  private static final Duration REFRESH_TOKEN_LIFETIME = Duration.ofDays(30);

  private final AuthService authService;

  @PostMapping("/login")
  public ResponseEntity<AuthAccessResponse> login(@RequestBody @Valid LoginRequest request) {

    return authResponse(
        authService.login(request.username(), request.password()));
  }

  @PostMapping("/register")
  public ResponseEntity<AuthAccessResponse> register(@RequestBody @Valid RegisterRequest request) {

    return authResponse(
        authService.register(request.username(), request.password()));
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthAccessResponse> refresh(
      @CookieValue(value = REFRESH_COOKIE, required = false) String refreshToken) {

    return authResponse(authService.refresh(refreshToken));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout() {

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, createRefreshCookie("", Duration.ZERO).toString())
        .build();
  }

  private ResponseCookie createRefreshCookie(String value, Duration maxAge) {
    return ResponseCookie.from(REFRESH_COOKIE, value == null ? "" : value)
        .httpOnly(true)
        .secure(false) // true в production
        .path(COOKIE_PATH)
        .sameSite(SAME_SITE)
        .maxAge(maxAge)
        .build();
  }

  private ResponseEntity<AuthAccessResponse> authResponse(AuthResult auth) {
    return ResponseEntity.ok()
        .header(
            HttpHeaders.SET_COOKIE,
            createRefreshCookie(
                auth.refreshToken(),
                REFRESH_TOKEN_LIFETIME)
                .toString())
        .body(new AuthAccessResponse(auth.accessToken()));
  }
}
