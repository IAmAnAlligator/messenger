package com.jeannimi.messenger.application.auth.service;

import static com.jeannimi.messenger.application.auth.AuthTokenConstants.REFRESH_TOKEN_TYPE;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jeannimi.messenger.application.auth.dto.AuthResult;
import com.jeannimi.messenger.application.exception.ConflictException;
import com.jeannimi.messenger.application.exception.UnauthorizedException;
import com.jeannimi.messenger.application.port.out.HandleGeneratorPort;
import com.jeannimi.messenger.application.port.out.IdGenerator;
import com.jeannimi.messenger.application.port.out.PasswordHashPort;
import com.jeannimi.messenger.application.port.out.TokenServicePort;
import com.jeannimi.messenger.application.port.out.UserRepositoryPort;
import com.jeannimi.messenger.domain.user.Handle;
import com.jeannimi.messenger.domain.user.Password;
import com.jeannimi.messenger.domain.user.PasswordHash;
import com.jeannimi.messenger.domain.user.User;
import com.jeannimi.messenger.domain.user.UserId;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  private static final String EMAIL = "alex@example.com";
  private static final String PASSWORD = "Password123";
  private static final String ACCESS_TOKEN = "access-token";
  private static final String REFRESH_TOKEN = "refresh-token";

  private static final UUID USER_UUID = UUID.fromString("01912345-6789-7123-8123-123456789abc");

  @Mock private UserRepositoryPort userRepository;

  @Mock private PasswordHashPort passwordHashPort;

  @Mock private TokenServicePort tokenService;

  @Mock private HandleGeneratorPort handleGenerator;

  @Mock private IdGenerator idGenerator;

  @Mock private User user;

  @Mock private PasswordHash passwordHash;

  @InjectMocks private AuthService authService;

  private UserId userId;

  @BeforeEach
  void setUp() {
    userId = new UserId(USER_UUID);
  }

  // login

  @Test
  void login_shouldReturnAuthResult_whenCredentialsAreValid() {
    when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

    when(user.getPasswordHash()).thenReturn(passwordHash);

    when(passwordHashPort.matches(any(Password.class), eq(passwordHash))).thenReturn(true);

    when(tokenService.generateAccessToken(user)).thenReturn(ACCESS_TOKEN);

    when(tokenService.generateRefreshToken(user)).thenReturn(REFRESH_TOKEN);

    AuthResult result = authService.login(EMAIL, PASSWORD);

    assertNotNull(result);

    verify(userRepository).findByEmail(EMAIL);
    verify(passwordHashPort).matches(any(Password.class), eq(passwordHash));
    verify(tokenService).generateAccessToken(user);
    verify(tokenService).generateRefreshToken(user);
  }

  @Test
  void login_shouldThrowUnauthorizedException_whenUserDoesNotExist() {
    when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

    assertThrows(UnauthorizedException.class, () -> authService.login(EMAIL, PASSWORD));

    verify(passwordHashPort, never()).matches(any(Password.class), any(PasswordHash.class));

    verify(tokenService, never()).generateAccessToken(any(User.class));

    verify(tokenService, never()).generateRefreshToken(any(User.class));
  }

  @Test
  void login_shouldThrowUnauthorizedException_whenPasswordIsIncorrect() {
    when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

    when(user.getPasswordHash()).thenReturn(passwordHash);

    when(passwordHashPort.matches(any(Password.class), eq(passwordHash))).thenReturn(false);

    assertThrows(UnauthorizedException.class, () -> authService.login(EMAIL, PASSWORD));

    verify(tokenService, never()).generateAccessToken(any(User.class));

    verify(tokenService, never()).generateRefreshToken(any(User.class));
  }

  // register

  @Test
  void register_shouldSaveUserAndReturnAuthResult_whenEmailIsAvailable() {
    when(userRepository.existsByEmail(EMAIL)).thenReturn(false);

    when(handleGenerator.generate()).thenReturn(new Handle("abc1234"));

    when(idGenerator.generate()).thenReturn(USER_UUID);

    when(passwordHashPort.hash(any(Password.class))).thenReturn(passwordHash);

    when(tokenService.generateAccessToken(any(User.class))).thenReturn(ACCESS_TOKEN);

    when(tokenService.generateRefreshToken(any(User.class))).thenReturn(REFRESH_TOKEN);

    AuthResult result = authService.register("Alex", EMAIL, PASSWORD);

    assertNotNull(result);

    verify(userRepository).existsByEmail(EMAIL);
    verify(handleGenerator).generate();
    verify(idGenerator).generate();
    verify(passwordHashPort).hash(any(Password.class));
    verify(userRepository).save(any(User.class));

    verify(tokenService).generateAccessToken(any(User.class));
    verify(tokenService).generateRefreshToken(any(User.class));
  }

  @Test
  void register_shouldThrowConflictException_whenEmailAlreadyExists() {
    when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

    assertThrows(ConflictException.class, () -> authService.register("Alex", EMAIL, PASSWORD));

    verify(userRepository, never()).save(any(User.class));

    verify(handleGenerator, never()).generate();

    verify(idGenerator, never()).generate();

    verify(passwordHashPort, never()).hash(any(Password.class));

    verify(tokenService, never()).generateAccessToken(any(User.class));

    verify(tokenService, never()).generateRefreshToken(any(User.class));
  }

  // refresh

  @Test
  void refresh_shouldReturnAuthResult_whenTokenIsValid() {
    when(tokenService.isTokenValid(REFRESH_TOKEN)).thenReturn(true);

    when(tokenService.extractTokenType(REFRESH_TOKEN)).thenReturn(REFRESH_TOKEN_TYPE);

    when(tokenService.extractUserId(REFRESH_TOKEN)).thenReturn(userId);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    when(tokenService.generateAccessToken(user)).thenReturn(ACCESS_TOKEN);

    when(tokenService.generateRefreshToken(user)).thenReturn(REFRESH_TOKEN);

    AuthResult result = authService.refresh(REFRESH_TOKEN);

    assertNotNull(result);

    verify(tokenService).isTokenValid(REFRESH_TOKEN);
    verify(tokenService).extractTokenType(REFRESH_TOKEN);
    verify(tokenService).extractUserId(REFRESH_TOKEN);
    verify(userRepository).findById(userId);

    verify(tokenService).generateAccessToken(user);
    verify(tokenService).generateRefreshToken(user);
  }

  @Test
  void refresh_shouldThrowUnauthorizedException_whenTokenIsNull() {
    assertThrows(UnauthorizedException.class, () -> authService.refresh(null));

    verify(tokenService, never()).isTokenValid(any(String.class));

    verify(tokenService, never()).generateAccessToken(any(User.class));
  }

  @Test
  void refresh_shouldThrowUnauthorizedException_whenTokenIsBlank() {
    assertThrows(UnauthorizedException.class, () -> authService.refresh("   "));

    verify(tokenService, never()).isTokenValid(any(String.class));

    verify(tokenService, never()).generateAccessToken(any(User.class));
  }

  @Test
  void refresh_shouldThrowUnauthorizedException_whenTokenIsInvalid() {
    when(tokenService.isTokenValid(REFRESH_TOKEN)).thenReturn(false);

    assertThrows(UnauthorizedException.class, () -> authService.refresh(REFRESH_TOKEN));

    verify(tokenService, never()).extractTokenType(any(String.class));

    verify(tokenService, never()).extractUserId(any(String.class));

    verify(userRepository, never()).findById(any(UserId.class));

    verify(tokenService, never()).generateAccessToken(any(User.class));
  }

  @Test
  void refresh_shouldThrowUnauthorizedException_whenTokenTypeIsIncorrect() {
    when(tokenService.isTokenValid(REFRESH_TOKEN)).thenReturn(true);

    when(tokenService.extractTokenType(REFRESH_TOKEN)).thenReturn("ACCESS");

    assertThrows(UnauthorizedException.class, () -> authService.refresh(REFRESH_TOKEN));

    verify(tokenService, never()).extractUserId(any(String.class));

    verify(userRepository, never()).findById(any(UserId.class));

    verify(tokenService, never()).generateAccessToken(any(User.class));
  }

  @Test
  void refresh_shouldThrowUnauthorizedException_whenUserDoesNotExist() {
    when(tokenService.isTokenValid(REFRESH_TOKEN)).thenReturn(true);

    when(tokenService.extractTokenType(REFRESH_TOKEN)).thenReturn(REFRESH_TOKEN_TYPE);

    when(tokenService.extractUserId(REFRESH_TOKEN)).thenReturn(userId);

    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(UnauthorizedException.class, () -> authService.refresh(REFRESH_TOKEN));

    verify(tokenService, never()).generateAccessToken(any(User.class));

    verify(tokenService, never()).generateRefreshToken(any(User.class));
  }
}
