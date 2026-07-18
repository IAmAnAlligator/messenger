package com.jeannimi.messenger.controller;

import com.jeannimi.messenger.dto.AuthAccessResponse;
import com.jeannimi.messenger.dto.AuthResponse;
import com.jeannimi.messenger.dto.LoginRequest;
import com.jeannimi.messenger.dto.RegisterRequest;
import com.jeannimi.messenger.service.AuthService;
import jakarta.servlet.http.HttpServletResponse;
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

  private final AuthService authService;

  // =========================
  // LOGIN
  // =========================
  @PostMapping("/login")
  public ResponseEntity<AuthAccessResponse> login(
      @RequestBody LoginRequest request, HttpServletResponse response) {

    AuthResponse auth = authService.login(request);

    setRefreshCookie(response, auth.refreshToken(), Duration.ofDays(30).getSeconds());

    return ResponseEntity.ok(new AuthAccessResponse(auth.accessToken()));
  }

  // =========================
  // REGISTER
  // =========================
  @PostMapping("/register")
  public ResponseEntity<AuthAccessResponse> register(
      @RequestBody RegisterRequest request, HttpServletResponse response) {

    AuthResponse auth = authService.register(request);

    setRefreshCookie(response, auth.refreshToken(), Duration.ofDays(30).getSeconds());

    return ResponseEntity.ok(new AuthAccessResponse(auth.accessToken()));
  }

  // =========================
  // REFRESH
  // =========================
  @PostMapping("/refresh")
  public ResponseEntity<AuthAccessResponse> refresh(
      @CookieValue(value = "refreshToken", required = false) String refreshToken,
      HttpServletResponse response) {

    AuthResponse auth = authService.refresh(refreshToken);

    setRefreshCookie(response, auth.refreshToken(), Duration.ofDays(30).getSeconds());

    return ResponseEntity.ok(new AuthAccessResponse(auth.accessToken()));
  }

  // =========================
  // LOGOUT
  // =========================
  @PostMapping("/logout")
  public ResponseEntity<Void> logout(HttpServletResponse response) {

    setRefreshCookie(
        response, "", 0 // 👈 удаление cookie
        );

    return ResponseEntity.ok().build();
  }

  // =========================
  // SINGLE COOKIE METHOD
  // =========================
  private void setRefreshCookie(HttpServletResponse response, String value, long maxAgeSeconds) {

    ResponseCookie cookie =
        ResponseCookie.from("refreshToken", value == null ? "" : value)
            .httpOnly(true)
            .secure(false) // true в production
            .path("/")
            .sameSite("Lax")
            .maxAge(maxAgeSeconds)
            .build();

    response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
  }
}
