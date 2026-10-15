package com.jeannimi.messenger.application.auth.service;

import static com.jeannimi.messenger.application.auth.AuthTokenConstants.REFRESH_TOKEN_TYPE;
import static com.jeannimi.messenger.application.auth.dto.RotationResult.REUSE_DETECTED;
import static com.jeannimi.messenger.application.auth.dto.RotationResult.ROTATED;
import static com.jeannimi.messenger.domain.user.Role.USER;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

import com.jeannimi.messenger.application.auth.dto.AuthResult;
import com.jeannimi.messenger.application.auth.dto.RotationResult;
import com.jeannimi.messenger.application.exception.ConflictException;
import com.jeannimi.messenger.application.exception.UnauthorizedException;
import com.jeannimi.messenger.application.port.out.HandleGeneratorPort;
import com.jeannimi.messenger.application.port.out.IdGenerator;
import com.jeannimi.messenger.application.port.out.PasswordHashPort;
import com.jeannimi.messenger.application.port.out.RefreshTokenRepositoryPort;
import com.jeannimi.messenger.application.port.out.TokenServicePort;
import com.jeannimi.messenger.application.port.out.UserRepositoryPort;
import com.jeannimi.messenger.domain.auth.RefreshToken;
import com.jeannimi.messenger.domain.auth.SessionId;
import com.jeannimi.messenger.domain.user.Email;
import com.jeannimi.messenger.domain.user.Handle;
import com.jeannimi.messenger.domain.user.Password;
import com.jeannimi.messenger.domain.user.PasswordHash;
import com.jeannimi.messenger.domain.user.User;
import com.jeannimi.messenger.domain.user.UserId;
import com.jeannimi.messenger.domain.user.Username;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

  private static final UUID USER_UUID = UUID.fromString("0199a111-1111-7111-8111-111111111111");

  private static final UUID SESSION_UUID = UUID.fromString("0199a222-2222-7222-8222-222222222222");

  private static final UUID TOKEN_UUID = UUID.fromString("0199a333-3333-7333-8333-333333333333");

  private static final UUID REPLACEMENT_UUID =
      UUID.fromString("0199a444-4444-7444-8444-444444444444");

  private static final String EMAIL = "user@example.com";
  private static final String PASSWORD = "password123";
  private static final String ACCESS_TOKEN = "access-token";
  private static final String REFRESH_TOKEN = "refresh-token";
  private static final String NEW_REFRESH_TOKEN = "new-refresh-token";

  private static final Instant EXPIRATION = Instant.parse("2026-11-13T12:00:00Z");

  @Mock private UserRepositoryPort userRepository;

  @Mock private PasswordHashPort passwordHashPort;

  @Mock private TokenServicePort tokenService;

  @Mock private HandleGeneratorPort handleGenerator;

  @Mock private IdGenerator idGenerator;

  @Mock private RefreshTokenRepositoryPort refreshTokenRepository;

  @Mock private RefreshTokenRevocationService refreshTokenRevocationService;

  @InjectMocks private AuthService authService;

  private User user;
  private UserId userId;
  private PasswordHash passwordHash;

  @BeforeEach
  void setUp() {
    userId = new UserId(USER_UUID);
    passwordHash = new PasswordHash(PASSWORD);
    user =
        User.create(
            userId,
            new Handle("abc1234"),
            new Username("Test User"),
            new Email(EMAIL),
            passwordHash,
            USER);
  }

  // -------------------------------------------------------------------------
  // login
  // -------------------------------------------------------------------------

  @Test
  void login_shouldReturnTokens_whenCredentialsAreValid() {

    when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

    when(passwordHashPort.matches(any(Password.class), eq(passwordHash))).thenReturn(true);

    when(tokenService.generateAccessToken(user)).thenReturn(ACCESS_TOKEN);

    when(tokenService.generateRefreshToken(user)).thenReturn(REFRESH_TOKEN);

    when(tokenService.extractExpiration(REFRESH_TOKEN)).thenReturn(EXPIRATION);

    when(idGenerator.generate()).thenReturn(TOKEN_UUID, SESSION_UUID);

    AuthResult result = authService.login(EMAIL, PASSWORD);

    assertNotNull(result);
    assertEquals(ACCESS_TOKEN, result.accessToken());
    assertEquals(REFRESH_TOKEN, result.refreshToken());

    verify(userRepository).findByEmail(EMAIL);

    verify(passwordHashPort).matches(any(Password.class), eq(passwordHash));

    verify(refreshTokenRepository).save(any(RefreshToken.class));
  }

  @Test
  void login_shouldThrowUnauthorizedException_whenUserDoesNotExist() {
    when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.empty());

    assertThrows(UnauthorizedException.class, () -> authService.login(EMAIL, PASSWORD));

    verifyNoInteractions(passwordHashPort);
    verifyNoInteractions(tokenService);
    verifyNoInteractions(refreshTokenRepository);
  }

  @Test
  void login_shouldThrowUnauthorizedException_whenPasswordIsIncorrect() {
    when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

    when(passwordHashPort.matches(any(Password.class), eq(passwordHash))).thenReturn(false);

    assertThrows(UnauthorizedException.class, () -> authService.login(EMAIL, PASSWORD));

    verifyNoInteractions(tokenService);
    verifyNoInteractions(refreshTokenRepository);
  }

  //  // -------------------------------------------------------------------------
  //  // register
  //  // -------------------------------------------------------------------------

  @Test
  void register_shouldCreateUserAndReturnTokens_whenEmailIsAvailable() {
    String username = "Test User";

    when(userRepository.existsByEmail(EMAIL)).thenReturn(false);

    when(handleGenerator.generate()).thenReturn(new Handle("abc1234"));

    when(idGenerator.generate()).thenReturn(USER_UUID, TOKEN_UUID, SESSION_UUID);

    when(passwordHashPort.hash(any(Password.class))).thenReturn(passwordHash);

    when(tokenService.generateAccessToken(any(User.class))).thenReturn(ACCESS_TOKEN);

    when(tokenService.generateRefreshToken(any(User.class))).thenReturn(REFRESH_TOKEN);

    when(tokenService.extractExpiration(REFRESH_TOKEN)).thenReturn(EXPIRATION);

    AuthResult result = authService.register(username, EMAIL, PASSWORD);

    assertNotNull(result);
    assertEquals(ACCESS_TOKEN, result.accessToken());
    assertEquals(REFRESH_TOKEN, result.refreshToken());

    ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

    verify(userRepository).save(userCaptor.capture());

    User savedUser = userCaptor.getValue();

    assertNotNull(savedUser);
    assertEquals(USER, savedUser.getRole());

    verify(passwordHashPort).hash(any(Password.class));
    verify(refreshTokenRepository).save(any(RefreshToken.class));
  }

  @Test
  void register_shouldThrowConflictException_whenEmailAlreadyExists() {
    when(userRepository.existsByEmail(EMAIL)).thenReturn(true);

    assertThrows(ConflictException.class, () -> authService.register("Test User", EMAIL, PASSWORD));

    verify(userRepository, never()).save(any(User.class));
    verifyNoInteractions(handleGenerator);
    verifyNoInteractions(passwordHashPort);
    verifyNoInteractions(tokenService);
    verifyNoInteractions(refreshTokenRepository);
  }

  //  // -------------------------------------------------------------------------
  //  // refresh
  //  // -------------------------------------------------------------------------

  @Test
  void refresh_shouldRotateTokenAndPreserveSession_whenTokenIsValid() {
    RefreshToken current = mock(RefreshToken.class);

    String currentHash = hashToken(REFRESH_TOKEN);

    SessionId sessionId = new SessionId(SESSION_UUID);

    when(tokenService.isTokenValid(REFRESH_TOKEN)).thenReturn(true);

    when(tokenService.extractTokenType(REFRESH_TOKEN)).thenReturn(REFRESH_TOKEN_TYPE);

    when(tokenService.extractUserId(REFRESH_TOKEN)).thenReturn(userId);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    when(refreshTokenRepository.findByTokenHash(currentHash)).thenReturn(Optional.of(current));

    when(current.getUserId()).thenReturn(userId);
    when(current.getSessionId()).thenReturn(sessionId);

    when(tokenService.generateRefreshToken(user)).thenReturn(NEW_REFRESH_TOKEN);

    when(tokenService.extractExpiration(NEW_REFRESH_TOKEN)).thenReturn(EXPIRATION);

    when(tokenService.generateAccessToken(user)).thenReturn(ACCESS_TOKEN);

    when(idGenerator.generate()).thenReturn(REPLACEMENT_UUID);

    when(refreshTokenRepository.rotate(
            eq(currentHash), any(RefreshToken.class), any(Instant.class)))
        .thenReturn(ROTATED);

    AuthResult result = authService.refresh(REFRESH_TOKEN);

    assertNotNull(result);
    assertEquals(ACCESS_TOKEN, result.accessToken());
    assertEquals(NEW_REFRESH_TOKEN, result.refreshToken());

    ArgumentCaptor<RefreshToken> replacementCaptor = ArgumentCaptor.forClass(RefreshToken.class);

    verify(refreshTokenRepository)
        .rotate(eq(currentHash), replacementCaptor.capture(), any(Instant.class));

    RefreshToken replacement = replacementCaptor.getValue();

    assertEquals(sessionId, replacement.getSessionId());
    assertEquals(userId, replacement.getUserId());
    assertEquals(hashToken(NEW_REFRESH_TOKEN), replacement.getTokenHash());

    verify(refreshTokenRevocationService, never())
        .revokeSession(any(SessionId.class), any(Instant.class));
  }

  @Test
  void refresh_shouldThrowUnauthorizedException_whenTokenIsMissing() {
    assertThrows(UnauthorizedException.class, () -> authService.refresh(null));

    verifyNoInteractions(refreshTokenRepository);
    verifyNoInteractions(userRepository);
  }

  @Test
  void refresh_shouldThrowUnauthorizedException_whenTokenIsBlank() {
    assertThrows(UnauthorizedException.class, () -> authService.refresh(" "));

    verifyNoInteractions(refreshTokenRepository);
  }

  @Test
  void refresh_shouldThrowUnauthorizedException_whenTokenIsInvalid() {
    when(tokenService.isTokenValid(REFRESH_TOKEN)).thenReturn(false);

    assertThrows(UnauthorizedException.class, () -> authService.refresh(REFRESH_TOKEN));

    verify(tokenService, never()).extractTokenType(any(String.class));

    verifyNoInteractions(refreshTokenRepository);
  }

  @Test
  void refresh_shouldThrowUnauthorizedException_whenTokenTypeIsIncorrect() {
    when(tokenService.isTokenValid(REFRESH_TOKEN)).thenReturn(true);

    when(tokenService.extractTokenType(REFRESH_TOKEN)).thenReturn("ACCESS");

    assertThrows(UnauthorizedException.class, () -> authService.refresh(REFRESH_TOKEN));

    verifyNoInteractions(refreshTokenRepository);
  }

  @Test
  void refresh_shouldThrowUnauthorizedException_whenUserDoesNotExist() {
    when(tokenService.isTokenValid(REFRESH_TOKEN)).thenReturn(true);

    when(tokenService.extractTokenType(REFRESH_TOKEN)).thenReturn(REFRESH_TOKEN_TYPE);

    when(tokenService.extractUserId(REFRESH_TOKEN)).thenReturn(userId);

    when(userRepository.findById(userId)).thenReturn(Optional.empty());

    assertThrows(UnauthorizedException.class, () -> authService.refresh(REFRESH_TOKEN));

    verify(refreshTokenRepository, never()).findByTokenHash(any(String.class));
  }

  @Test
  void refresh_shouldThrowUnauthorizedException_whenTokenIsNotStored() {
    String currentHash = hashToken(REFRESH_TOKEN);

    when(tokenService.isTokenValid(REFRESH_TOKEN)).thenReturn(true);

    when(tokenService.extractTokenType(REFRESH_TOKEN)).thenReturn(REFRESH_TOKEN_TYPE);

    when(tokenService.extractUserId(REFRESH_TOKEN)).thenReturn(userId);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    when(refreshTokenRepository.findByTokenHash(currentHash)).thenReturn(Optional.empty());

    assertThrows(UnauthorizedException.class, () -> authService.refresh(REFRESH_TOKEN));

    verify(refreshTokenRepository, never())
        .rotate(any(String.class), any(RefreshToken.class), any(Instant.class));
  }

  @Test
  void refresh_shouldThrowUnauthorizedException_whenTokenBelongsToAnotherUser() {
    RefreshToken current = mock(RefreshToken.class);

    UserId anotherUserId = new UserId(UUID.fromString("0199a555-5555-7555-8555-555555555555"));

    String currentHash = hashToken(REFRESH_TOKEN);

    when(tokenService.isTokenValid(REFRESH_TOKEN)).thenReturn(true);

    when(tokenService.extractTokenType(REFRESH_TOKEN)).thenReturn(REFRESH_TOKEN_TYPE);

    when(tokenService.extractUserId(REFRESH_TOKEN)).thenReturn(userId);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    when(refreshTokenRepository.findByTokenHash(currentHash)).thenReturn(Optional.of(current));

    when(current.getUserId()).thenReturn(anotherUserId);

    assertThrows(UnauthorizedException.class, () -> authService.refresh(REFRESH_TOKEN));

    verify(refreshTokenRepository, never())
        .rotate(any(String.class), any(RefreshToken.class), any(Instant.class));

    verifyNoInteractions(refreshTokenRevocationService);
  }

  @Test
  void refresh_shouldRevokeSession_whenTokenReuseIsDetected() {
    RefreshToken current = mock(RefreshToken.class);

    String currentHash = hashToken(REFRESH_TOKEN);

    SessionId sessionId = new SessionId(SESSION_UUID);

    when(tokenService.isTokenValid(REFRESH_TOKEN)).thenReturn(true);

    when(tokenService.extractTokenType(REFRESH_TOKEN)).thenReturn(REFRESH_TOKEN_TYPE);

    when(tokenService.extractUserId(REFRESH_TOKEN)).thenReturn(userId);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    when(refreshTokenRepository.findByTokenHash(currentHash)).thenReturn(Optional.of(current));

    when(current.getUserId()).thenReturn(userId);
    when(current.getSessionId()).thenReturn(sessionId);

    when(tokenService.generateRefreshToken(user)).thenReturn(NEW_REFRESH_TOKEN);

    when(tokenService.extractExpiration(NEW_REFRESH_TOKEN)).thenReturn(EXPIRATION);

    when(idGenerator.generate()).thenReturn(REPLACEMENT_UUID);

    when(refreshTokenRepository.rotate(
            eq(currentHash), any(RefreshToken.class), any(Instant.class)))
        .thenReturn(REUSE_DETECTED);

    assertThrows(UnauthorizedException.class, () -> authService.refresh(REFRESH_TOKEN));

    verify(refreshTokenRevocationService).revokeSession(eq(sessionId), any(Instant.class));

    verify(tokenService, never()).generateAccessToken(any(User.class));
  }

  @Test
  void refresh_shouldThrowUnauthorizedException_whenRotationFails() {
    RefreshToken current = mock(RefreshToken.class);

    String currentHash = hashToken(REFRESH_TOKEN);

    SessionId sessionId = new SessionId(SESSION_UUID);

    when(tokenService.isTokenValid(REFRESH_TOKEN)).thenReturn(true);

    when(tokenService.extractTokenType(REFRESH_TOKEN)).thenReturn(REFRESH_TOKEN_TYPE);

    when(tokenService.extractUserId(REFRESH_TOKEN)).thenReturn(userId);

    when(userRepository.findById(userId)).thenReturn(Optional.of(user));

    when(refreshTokenRepository.findByTokenHash(currentHash)).thenReturn(Optional.of(current));

    when(current.getUserId()).thenReturn(userId);
    when(current.getSessionId()).thenReturn(sessionId);

    when(tokenService.generateRefreshToken(user)).thenReturn(NEW_REFRESH_TOKEN);

    when(tokenService.extractExpiration(NEW_REFRESH_TOKEN)).thenReturn(EXPIRATION);

    when(idGenerator.generate()).thenReturn(REPLACEMENT_UUID);

    when(refreshTokenRepository.rotate(
            eq(currentHash), any(RefreshToken.class), any(Instant.class)))
        .thenReturn(RotationResult.NOT_FOUND);

    assertThrows(UnauthorizedException.class, () -> authService.refresh(REFRESH_TOKEN));

    verify(refreshTokenRevocationService, never())
        .revokeSession(any(SessionId.class), any(Instant.class));
  }

  @Test
  void login_shouldStoreSha256HashOfRefreshToken() throws Exception {
    when(userRepository.findByEmail(EMAIL)).thenReturn(Optional.of(user));

    when(passwordHashPort.matches(any(Password.class), eq(passwordHash))).thenReturn(true);

    when(tokenService.generateAccessToken(user)).thenReturn(ACCESS_TOKEN);

    when(tokenService.generateRefreshToken(user)).thenReturn(REFRESH_TOKEN);

    when(tokenService.extractExpiration(REFRESH_TOKEN)).thenReturn(EXPIRATION);

    when(idGenerator.generate()).thenReturn(TOKEN_UUID, SESSION_UUID);

    authService.login(EMAIL, PASSWORD);

    ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);

    verify(refreshTokenRepository).save(captor.capture());

    String expectedHash =
        HexFormat.of()
            .formatHex(
                MessageDigest.getInstance("SHA-256")
                    .digest(REFRESH_TOKEN.getBytes(StandardCharsets.UTF_8)));

    assertEquals(expectedHash, captor.getValue().getTokenHash());
  }

  //  // -------------------------------------------------------------------------
  //  // logout
  //  // -------------------------------------------------------------------------

  @Test
  void logout_shouldRevokeSession_whenRefreshTokenExists() {
    RefreshToken storedToken = mock(RefreshToken.class);

    String tokenHash = hashToken(REFRESH_TOKEN);

    SessionId sessionId = new SessionId(SESSION_UUID);

    when(tokenService.isTokenValid(REFRESH_TOKEN)).thenReturn(true);

    when(tokenService.extractTokenType(REFRESH_TOKEN)).thenReturn(REFRESH_TOKEN_TYPE);

    when(refreshTokenRepository.findByTokenHash(tokenHash)).thenReturn(Optional.of(storedToken));

    when(storedToken.getSessionId()).thenReturn(sessionId);

    authService.logout(REFRESH_TOKEN);

    verify(refreshTokenRepository).revokeSession(eq(sessionId), any(Instant.class));
  }

  @Test
  void logout_shouldDoNothing_whenTokenIsNull() {
    authService.logout(null);

    verifyNoInteractions(tokenService);
    verifyNoInteractions(refreshTokenRepository);
  }

  @Test
  void logout_shouldDoNothing_whenTokenIsBlank() {
    authService.logout(" ");

    verifyNoInteractions(tokenService);
    verifyNoInteractions(refreshTokenRepository);
  }

  @Test
  void logout_shouldDoNothing_whenTokenIsInvalid() {
    when(tokenService.isTokenValid(REFRESH_TOKEN)).thenReturn(false);

    authService.logout(REFRESH_TOKEN);

    verify(tokenService, never()).extractTokenType(any(String.class));

    verifyNoInteractions(refreshTokenRepository);
  }

  @Test
  void logout_shouldDoNothing_whenTokenTypeIsIncorrect() {
    when(tokenService.isTokenValid(REFRESH_TOKEN)).thenReturn(true);

    when(tokenService.extractTokenType(REFRESH_TOKEN)).thenReturn("ACCESS");

    authService.logout(REFRESH_TOKEN);

    verifyNoInteractions(refreshTokenRepository);
  }

  @Test
  void logout_shouldDoNothing_whenTokenIsNotStored() {
    when(tokenService.isTokenValid(REFRESH_TOKEN)).thenReturn(true);

    when(tokenService.extractTokenType(REFRESH_TOKEN)).thenReturn(REFRESH_TOKEN_TYPE);

    when(refreshTokenRepository.findByTokenHash(hashToken(REFRESH_TOKEN)))
        .thenReturn(Optional.empty());

    authService.logout(REFRESH_TOKEN);

    verify(refreshTokenRepository, never()).revokeSession(any(SessionId.class), any(Instant.class));
  }

  //  // -------------------------------------------------------------------------
  //  // logoutAll
  //  // -------------------------------------------------------------------------

  @Test
  void logoutAll_shouldRevokeAllActiveTokensForUser() {
    authService.logoutAll(userId);

    verify(refreshTokenRepository).revokeAllActiveByUserId(eq(userId), any(Instant.class));
  }

  @Test
  void logoutAll_shouldThrowIllegalArgumentException_whenUserIdIsNull() {
    assertThrows(IllegalArgumentException.class, () -> authService.logoutAll(null));

    verifyNoInteractions(refreshTokenRepository);
  }

  // -------------------------------------------------------------------------
  // Test helpers
  // -------------------------------------------------------------------------

  private String hashToken(String token) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");

      byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));

      return HexFormat.of().formatHex(hash);

    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 algorithm is unavailable", exception);
    }
  }
}
