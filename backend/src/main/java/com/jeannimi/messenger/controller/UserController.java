package com.jeannimi.messenger.controller;

import com.jeannimi.messenger.dto.CustomUserDetails;
import com.jeannimi.messenger.dto.UserDto;
import com.jeannimi.messenger.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @GetMapping("/me")
  public UserDto me(@AuthenticationPrincipal CustomUserDetails userId) {
    return userService.getCurrentUser(userId.getId());
  }
}
