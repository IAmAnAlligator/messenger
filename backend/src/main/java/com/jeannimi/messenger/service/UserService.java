package com.jeannimi.messenger.service;

import com.jeannimi.messenger.dto.UserDto;
import com.jeannimi.messenger.entity.User;
import com.jeannimi.messenger.exception_handling.NotFoundException;
import com.jeannimi.messenger.repository.UserRepository;
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
}
