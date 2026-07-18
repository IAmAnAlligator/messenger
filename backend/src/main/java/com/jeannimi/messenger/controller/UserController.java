package com.jeannimi.messenger.controller;

import com.jeannimi.messenger.dto.CustomUserDetails;
import com.jeannimi.messenger.dto.UserDto;
import com.jeannimi.messenger.service.UserService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

  private final UserService userService;

  @GetMapping("/me")
  public UserDto me(@AuthenticationPrincipal CustomUserDetails userId) {
    return userService.getCurrentUser(userId.id());
  }

  @GetMapping("/search")
  public List<UserDto> searchUsers(
      @RequestParam String query, @AuthenticationPrincipal CustomUserDetails user) {
    return userService.searchUsers(query, user.id());
  }
}
