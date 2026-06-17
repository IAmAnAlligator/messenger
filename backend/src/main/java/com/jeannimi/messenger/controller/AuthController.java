package com.jeannimi.messenger.controller;

import com.jeannimi.messenger.dto.AuthResponse;
import com.jeannimi.messenger.dto.LoginRequest;
import com.jeannimi.messenger.dto.RefreshRequest;
import com.jeannimi.messenger.dto.RegisterRequest;
import com.jeannimi.messenger.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

  private final AuthService authService;

  @PostMapping("/login")
  public AuthResponse login(@RequestBody LoginRequest request) {
    return authService.login(request);
  }

  @PostMapping("/register")
  public AuthResponse register(@RequestBody RegisterRequest request) {
    return authService.register(request);
  }

  @PostMapping("/refresh")
  public AuthResponse refresh(@RequestBody RefreshRequest request) {
    return authService.refresh(request.getRefreshToken());
  }
}
