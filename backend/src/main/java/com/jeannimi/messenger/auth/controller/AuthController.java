package com.jeannimi.messenger.auth.controller;

import com.jeannimi.messenger.auth.dto.AuthAccessResponse;
import com.jeannimi.messenger.auth.dto.AuthResponse;
import com.jeannimi.messenger.auth.dto.LoginRequest;
import com.jeannimi.messenger.auth.dto.RegisterRequest;
import com.jeannimi.messenger.auth.service.AuthService;
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

    AuthResponse auth = authService.login(request);

    return ResponseEntity.ok()
        .header(
            HttpHeaders.SET_COOKIE,
            createRefreshCookie(auth.refreshToken(), REFRESH_TOKEN_LIFETIME.getSeconds())
                .toString())
        .body(new AuthAccessResponse(auth.accessToken()));
  }

  @PostMapping("/register")
  public ResponseEntity<AuthAccessResponse> register(@RequestBody @Valid RegisterRequest request) {

    AuthResponse auth = authService.register(request);

    return ResponseEntity.ok()
        .header(
            HttpHeaders.SET_COOKIE,
            createRefreshCookie(auth.refreshToken(), REFRESH_TOKEN_LIFETIME.getSeconds())
                .toString())
        .body(new AuthAccessResponse(auth.accessToken()));
  }

  @PostMapping("/refresh")
  public ResponseEntity<AuthAccessResponse> refresh(
      @CookieValue(value = REFRESH_COOKIE, required = false) String refreshToken) {

    AuthResponse auth = authService.refresh(refreshToken);

    return ResponseEntity.ok()
        .header(
            HttpHeaders.SET_COOKIE,
            createRefreshCookie(auth.refreshToken(), REFRESH_TOKEN_LIFETIME.getSeconds())
                .toString())
        .body(new AuthAccessResponse(auth.accessToken()));
  }

  @PostMapping("/logout")
  public ResponseEntity<Void> logout() {

    return ResponseEntity.ok()
        .header(HttpHeaders.SET_COOKIE, createRefreshCookie("", 0).toString())
        .build();
  }

  private ResponseCookie createRefreshCookie(String value, long maxAgeSeconds) {
    return ResponseCookie.from(REFRESH_COOKIE, value == null ? "" : value)
        .httpOnly(true)
        .secure(false) // true в production
        .path(COOKIE_PATH)
        .sameSite(SAME_SITE)
        .maxAge(maxAgeSeconds)
        .build();
  }
}
