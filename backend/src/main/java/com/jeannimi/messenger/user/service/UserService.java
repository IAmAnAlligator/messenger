package com.jeannimi.messenger.user.service;

import com.jeannimi.messenger.application.port.out.UserRepositoryPort;
import com.jeannimi.messenger.application.user.dto.UserResult;
import com.jeannimi.messenger.common.exception_handling.NotFoundException;
import com.jeannimi.messenger.domain.user.User;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepositoryPort userRepository;

  public UserResult getCurrentUser(Long userId) {

    User user =
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

    return toResult(user);
  }

  public List<UserResult> searchUsers(String query, Long currentUserId) {

    if (query == null || query.trim().length() < 2) {
      return List.of();
    }

    List<User> users = userRepository.searchByUsername(query);

    return users.stream()
        .filter(u -> !u.getId().equals(currentUserId)) // исключаем себя
        .map(this::toResult)
        .toList();
  }

  private UserResult toResult(User user) {

    return new UserResult(user.getId(), user.getUsername().getValue(), user.getRole());
  }
}
