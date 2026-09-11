package com.jeannimi.messenger.adapter.in.web.user;

import com.jeannimi.messenger.application.user.dto.UserResult;
import com.jeannimi.messenger.adapter.in.security.CustomUserDetails;
import com.jeannimi.messenger.adapter.in.web.user.dto.UserDto;
import com.jeannimi.messenger.application.user.service.UserService;
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
  public UserDto me(@AuthenticationPrincipal CustomUserDetails user) {
    UserResult result = userService.getCurrentUser(user.id());
    return toDto(result);
  }

  @GetMapping("/search")
  public List<UserDto> searchUsers(
      @RequestParam @NotBlank String query, @AuthenticationPrincipal CustomUserDetails user) {

    return userService.searchUsers(query, user.id()).stream().map(this::toDto).toList();
  }

  private UserDto toDto(UserResult result) {
    return new UserDto(result.id(), result.username(), result.role());
  }
}
