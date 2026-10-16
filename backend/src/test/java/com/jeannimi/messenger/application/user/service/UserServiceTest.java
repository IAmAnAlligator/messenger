package com.jeannimi.messenger.application.user.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import com.jeannimi.messenger.application.exception.NotFoundException;
import com.jeannimi.messenger.application.port.out.UserRepositoryPort;
import com.jeannimi.messenger.application.user.dto.UserResult;
import com.jeannimi.messenger.domain.user.Email;
import com.jeannimi.messenger.domain.user.Handle;
import com.jeannimi.messenger.domain.user.PasswordHash;
import com.jeannimi.messenger.domain.user.Role;
import com.jeannimi.messenger.domain.user.User;
import com.jeannimi.messenger.domain.user.UserId;
import com.jeannimi.messenger.domain.user.Username;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

  private static final UUID USER_UUID = UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789012");

  private static final UUID SECOND_USER_UUID =
      UUID.fromString("0199e8a1-7b2c-7abc-8def-123456789013");

  @Mock private UserRepositoryPort userRepository;

  @InjectMocks private UserService userService;

  private UserId currentUserId;
  private User user;
  private UserResult userResult;

  @BeforeEach
  void setUp() {
    currentUserId = new UserId(USER_UUID);

    Handle handle = new Handle("abc1234");
    Username username = new Username("Alex");

    user =
        User.create(
            currentUserId,
            handle,
            username,
            new Email("alex@mail.com"),
            new PasswordHash("password123"),
            Role.USER);

    userResult = new UserResult(currentUserId, handle.getValue(), username.getValue(), Role.USER);
  }

  @Test
  void getCurrentUser_shouldReturnUserResult_whenUserExists() {
    when(userRepository.findById(currentUserId)).thenReturn(Optional.of(user));

    UserResult result = userService.getCurrentUser(currentUserId);

    assertEquals(userResult, result);

    verify(userRepository).findById(currentUserId);
    verifyNoMoreInteractions(userRepository);
  }

  @Test
  void getCurrentUser_shouldThrowNotFoundException_whenUserDoesNotExist() {
    when(userRepository.findById(currentUserId)).thenReturn(Optional.empty());

    assertThrows(NotFoundException.class, () -> userService.getCurrentUser(currentUserId));

    verify(userRepository).findById(currentUserId);
    verifyNoMoreInteractions(userRepository);
  }

  @Test
  void searchUsers_shouldSearchByHandle_whenQueryContainsFullHandle() {
    String query = "@abc1234";

    when(userRepository.searchByHandle("abc1234", currentUserId)).thenReturn(List.of(user));

    List<UserResult> result = userService.searchUsers(query, currentUserId);

    assertEquals(List.of(userResult), result);

    verify(userRepository).searchByHandle("abc1234", currentUserId);
    verifyNoMoreInteractions(userRepository);
  }

  @Test
  void searchUsers_shouldTrimQuery_whenSearchingByHandle() {
    String query = "  @abc1234  ";

    when(userRepository.searchByHandle("abc1234", currentUserId)).thenReturn(List.of(user));

    List<UserResult> result = userService.searchUsers(query, currentUserId);

    assertEquals(List.of(userResult), result);

    verify(userRepository).searchByHandle("abc1234", currentUserId);
    verifyNoMoreInteractions(userRepository);
  }

  @Test
  void searchUsers_shouldSearchByUsername_whenQueryDoesNotContainFullHandle() {
    String query = "Alex";

    when(userRepository.searchByUsername(query, currentUserId)).thenReturn(List.of(user));

    List<UserResult> result = userService.searchUsers(query, currentUserId);

    assertEquals(List.of(userResult), result);

    verify(userRepository).searchByUsername(query, currentUserId);
    verifyNoMoreInteractions(userRepository);
  }

  @Test
  void searchUsers_shouldTrimQuery_whenSearchingByUsername() {
    String query = "  Alex  ";

    when(userRepository.searchByUsername("Alex", currentUserId)).thenReturn(List.of(user));

    List<UserResult> result = userService.searchUsers(query, currentUserId);

    assertEquals(List.of(userResult), result);

    verify(userRepository).searchByUsername("Alex", currentUserId);
    verifyNoMoreInteractions(userRepository);
  }

  @Test
  void searchUsers_shouldSearchByUsername_whenHandleLengthIsIncorrect() {
    String query = "@abc";

    when(userRepository.searchByUsername(query, currentUserId)).thenReturn(List.of(user));

    List<UserResult> result = userService.searchUsers(query, currentUserId);

    assertEquals(List.of(userResult), result);

    verify(userRepository).searchByUsername(query, currentUserId);
    verify(userRepository, never()).searchByHandle(anyString(), any());
    verifyNoMoreInteractions(userRepository);
  }

  @Test
  void searchUsers_shouldSearchByUsername_whenQueryDoesNotStartWithAt() {
    String query = "abcdefg1";

    when(userRepository.searchByUsername(query, currentUserId)).thenReturn(List.of(user));

    List<UserResult> result = userService.searchUsers(query, currentUserId);

    assertEquals(List.of(userResult), result);

    verify(userRepository).searchByUsername(query, currentUserId);
    verify(userRepository, never()).searchByHandle(anyString(), any());
    verifyNoMoreInteractions(userRepository);
  }

  @Test
  void searchUsers_shouldSearchByUsername_whenHandleIsIncomplete() {
    String query = "@abc";

    when(userRepository.searchByUsername(query, currentUserId)).thenReturn(List.of(user));

    List<UserResult> result = userService.searchUsers(query, currentUserId);

    assertEquals(List.of(userResult), result);

    verify(userRepository).searchByUsername(query, currentUserId);
    verifyNoMoreInteractions(userRepository);
  }

  @Test
  void searchUsers_shouldSearchByUsername_whenHandleContainsInvalidCharacters() {
    String query = "@abc!234";

    when(userRepository.searchByUsername(query, currentUserId)).thenReturn(List.of(user));

    List<UserResult> result = userService.searchUsers(query, currentUserId);

    assertEquals(List.of(userResult), result);

    verify(userRepository).searchByUsername(query, currentUserId);
    verifyNoMoreInteractions(userRepository);
  }

  @Test
  void searchUsers_shouldReturnEmptyList_whenNoUsersFoundByHandle() {
    when(userRepository.searchByHandle("abc1234", currentUserId)).thenReturn(List.of());

    List<UserResult> result = userService.searchUsers("@abc1234", currentUserId);

    assertEquals(List.of(), result);

    verify(userRepository).searchByHandle("abc1234", currentUserId);
    verifyNoMoreInteractions(userRepository);
  }

  @Test
  void searchUsers_shouldReturnEmptyList_whenNoUsersFoundByUsername() {
    when(userRepository.searchByUsername("Nobody", currentUserId)).thenReturn(List.of());

    List<UserResult> result = userService.searchUsers("Nobody", currentUserId);

    assertEquals(List.of(), result);

    verify(userRepository).searchByUsername("Nobody", currentUserId);
    verifyNoMoreInteractions(userRepository);
  }

  @Test
  void searchUsers_shouldMapAllUsersToResults() {
    UserId secondUserId = new UserId(SECOND_USER_UUID);
    Handle secondHandle = new Handle("xyz5678");
    Username secondUsername = new Username("Bob");

    User secondUser =
        User.create(
            secondUserId,
            secondHandle,
            secondUsername,
            new Email("bob@mail.com"),
            new PasswordHash("password456"),
            Role.USER);

    UserResult secondUserResult =
        new UserResult(secondUserId, secondHandle.getValue(), secondUsername.getValue(), Role.USER);

    when(userRepository.searchByUsername("Alex", currentUserId))
        .thenReturn(List.of(user, secondUser));

    List<UserResult> result = userService.searchUsers("Alex", currentUserId);

    assertEquals(List.of(userResult, secondUserResult), result);

    verify(userRepository).searchByUsername("Alex", currentUserId);
    verifyNoMoreInteractions(userRepository);
  }
}
