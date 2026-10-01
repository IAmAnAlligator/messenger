package com.jeannimi.messenger.application.user.service;

import com.jeannimi.messenger.application.exception.NotFoundException;
import com.jeannimi.messenger.application.port.out.UserRepositoryPort;
import com.jeannimi.messenger.application.user.dto.UserResult;
import com.jeannimi.messenger.domain.user.Handle;
import com.jeannimi.messenger.domain.user.User;
import com.jeannimi.messenger.domain.user.UserId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepositoryPort userRepository;

  public UserResult getCurrentUser(UserId userId) {

    User user =
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

    return toResult(user);
  }

  public List<UserResult> searchUsers(String query, UserId currentUserId) {

    String normalizedQuery = query.trim();

    if (isFullHandleQuery(normalizedQuery)) {

      String handleValue = normalizedQuery.substring(1);

      Handle handle = new Handle(handleValue);

      return userRepository.searchByHandle(handle.getValue(), currentUserId).stream()
          .map(this::toResult)
          .toList();
    }

    return userRepository.searchByUsername(normalizedQuery, currentUserId).stream()
        .map(this::toResult)
        .toList();
  }

  private boolean isFullHandleQuery(String query) {

    if (query.length() != Handle.HANDLE_LENGTH + 1 || query.charAt(0) != '@') {
      return false;
    }

    try {
      new Handle(query.substring(1));
      return true;
    } catch (IllegalArgumentException e) {
      return false;
    }
  }

  private UserResult toResult(User user) {

    return new UserResult(
        user.getId(),
        user.getHandle().getValue(),
        user.getUsername().getValue(),
        user.getRole());
  }
}
