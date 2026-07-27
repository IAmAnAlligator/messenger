package com.jeannimi.messenger.user.controller;

import com.jeannimi.messenger.user.dto.CustomUserDetails;
import com.jeannimi.messenger.user.dto.UserDto;
import com.jeannimi.messenger.user.service.UserService;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Validated
public class UserController {

  private final UserService userService;

  @GetMapping("/me")
  public UserDto me(@AuthenticationPrincipal CustomUserDetails userId) {
    return userService.getCurrentUser(userId.id());
  }

  @GetMapping("/search")
  public List<UserDto> searchUsers(
      @RequestParam @NotBlank String query, @AuthenticationPrincipal CustomUserDetails user) {
    return userService.searchUsers(query, user.id());
  }
}
