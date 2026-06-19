package com.jeannimi.messenger.service;

import com.jeannimi.messenger.dto.UserDto;
import com.jeannimi.messenger.entity.User;
import com.jeannimi.messenger.exception_handling.NotFoundException;
import com.jeannimi.messenger.repository.UserRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

  private final UserRepository userRepository;

  public UserDto getCurrentUser(Long userId) {

    User user =
        userRepository.findById(userId).orElseThrow(() -> new NotFoundException("User not found"));

    return new UserDto(user.getId(), user.getUsername(), user.getRole());
  }

  public List<UserDto> searchUsers(String query, Long currentUserId) {

    if (query == null || query.trim().length() < 2) {
      return List.of();
    }

    List<User> users = userRepository.searchByUsername(query);

    return users.stream()
        .filter(u -> !u.getId().equals(currentUserId)) // исключаем себя
        .map(u -> new UserDto(u.getId(), u.getUsername(), u.getRole()))
        .toList();
  }
}
