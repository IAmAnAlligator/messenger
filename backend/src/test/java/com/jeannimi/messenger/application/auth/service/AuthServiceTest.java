package com.jeannimi.messenger.application.auth.service;

import static com.jeannimi.messenger.application.auth.AuthTokenConstants.REFRESH_TOKEN_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

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
import com.jeannimi.messenger.domain.auth.RefreshTokenId;
import com.jeannimi.messenger.domain.auth.SessionId;
import com.jeannimi.messenger.domain.user.Handle;
import com.jeannimi.messenger.domain.user.Password;
import com.jeannimi.messenger.domain.user.PasswordHash;
import com.jeannimi.messenger.domain.user.User;
import com.jeannimi.messenger.domain.user.UserId;
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

  private static final String EMAIL = "alex@example.com";
  private static final String PASSWORD = "Password123";

  private static final String ACCESS_TOKEN = "access-token";
  private static final String REFRESH_TOKEN = "refresh-token";
  private static final String NEW_ACCESS_TOKEN = "new-access-token";
  private static final String NEW_REFRESH_TOKEN = "new-refresh-token";

  private static final UUID USER_UUID =
      UUID.fromString("01912345-6789-7123-8123-123456789abc");

  private static final UUID REFRESH_UUID =
      UUID.fromString("01912345-6789-7123-8123-123456789abd");

  private static final UUID SESSION_UUID =
      UUID.fromString("01912345-6789-7123-8123-123456789abe");

  private static final UUID REPLACEMENT_UUID =
      UUID.fromString("01912345-6789-7123-8123-123456789abf");

  private static final Instant EXPIRATION =
      Instant.parse("2026-11-11T12:00:00Z");

  @Mock
  private UserRepositoryPort userRepository;

  @Mock
  private RefreshTokenRepositoryPort refreshTokenRepository;

  @Mock
  private PasswordHashPort passwordHashPort;

  @Mock
  private TokenServicePort tokenService;

  @Mock
  private HandleGeneratorPort handleGenerator;

  @Mock
  private IdGenerator idGenerator;

  @Mock
  private User user;

  @Mock
  private PasswordHash passwordHash;

  @InjectMocks
  private AuthService authService;

  private UserId userId;
  private SessionId sessionId;
  private RefreshToken currentRefreshToken;

  @BeforeEach
  void setUp() {
    userId = new UserId(USER_UUID);
    sessionId = new SessionId(SESSION_UUID);

    currentRefreshToken = RefreshToken.create(
        new RefreshTokenId(REFRESH_UUID),
        sessionId,
        userId,
        hash(REFRESH_TOKEN),
        Instant.parse("2026-10-12T12:00:00Z"),
        EXPIRATION
    );
  }

  @Test
  void login_shouldReturnAuthResult_whenCredentialsAreValid() {
    when(userRepository.findByEmail(EMAIL))
        .thenReturn(Optional.of(user));

    when(user.getId()).thenReturn(userId);
    when(user.getPasswordHash()).thenReturn(passwordHash);

    when(passwordHashPort.matches(any(Password.class), eq(passwordHash)))
        .thenReturn(true);

    stubGeneratedTokens(REFRESH_UUID, SESSION_UUID);

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
    when(userRepository.findByEmail(EMAIL))
        .thenReturn(Optional.empty());

    assertThrows(
        UnauthorizedException.class,
        () -> authService.login(EMAIL, PASSWORD)
    );

    verifyNoInteractions(tokenService);
    verifyNoInteractions(refreshTokenRepository);
  }

  @Test
  void login_shouldThrowUnauthorizedException_whenPasswordIsIncorrect() {
    when(userRepository.findByEmail(EMAIL))
        .thenReturn(Optional.of(user));

    when(user.getPasswordHash()).thenReturn(passwordHash);

    when(passwordHashPort.matches(any(Password.class), eq(passwordHash)))
        .thenReturn(false);

    assertThrows(
        UnauthorizedException.class,
        () -> authService.login(EMAIL, PASSWORD)
    );

    verifyNoInteractions(tokenService);
    verifyNoInteractions(refreshTokenRepository);
  }

  @Test
  void register_shouldSaveUserAndRefreshToken_whenEmailIsAvailable() {
    when(userRepository.existsByEmail(EMAIL))
        .thenReturn(false);

    when(handleGenerator.generate())
        .thenReturn(new Handle("abc1234"));

    when(idGenerator.generate())
        .thenReturn(USER_UUID, REFRESH_UUID, SESSION_UUID);

    when(passwordHashPort.hash(any(Password.class)))
        .thenReturn(passwordHash);

    when(tokenService.generateAccessToken(any(User.class)))
        .thenReturn(ACCESS_TOKEN);

    when(tokenService.generateRefreshToken(any(User.class)))
        .thenReturn(REFRESH_TOKEN);

    when(tokenService.extractExpiration(REFRESH_TOKEN))
        .thenReturn(EXPIRATION);

    AuthResult result = authService.register("Alex", EMAIL, PASSWORD);

    assertNotNull(result);
    assertEquals(ACCESS_TOKEN, result.accessToken());
    assertEquals(REFRESH_TOKEN, result.refreshToken());

    verify(userRepository).save(any(User.class));
    verify(refreshTokenRepository).save(any(RefreshToken.class));
  }

  @Test
  void register_shouldThrowConflictException_whenEmailAlreadyExists() {
    when(userRepository.existsByEmail(EMAIL))
        .thenReturn(true);

    assertThrows(
        ConflictException.class,
        () -> authService.register("Alex", EMAIL, PASSWORD)
    );

    verify(userRepository, never()).save(any(User.class));
    verify(refreshTokenRepository, never()).save(any(RefreshToken.class));
    verify(tokenService, never()).generateAccessToken(any(User.class));
    verify(tokenService, never()).generateRefreshToken(any(User.class));
  }

  @Test
  void refresh_shouldRotateTokenAndReturnAuthResult_whenTokenIsValid() {
    stubValidRefreshToken();
    stubReplacementToken();

    when(refreshTokenRepository.rotate(
        eq(hash(REFRESH_TOKEN)),
        any(RefreshToken.class),
        any(Instant.class)
    )).thenReturn(RotationResult.ROTATED);

    AuthResult result = authService.refresh(REFRESH_TOKEN);

    assertNotNull(result);
//    assertEquals(NEW_ACCESS_TOKEN, result.accessToken());
//    assertEquals(NEW_REFRESH_TOKEN, result.refreshToken());

    verify(refreshTokenRepository).rotate(
        eq(hash(REFRESH_TOKEN)),
        any(RefreshToken.class),
        any(Instant.class)
    );
  }

  @Test
  void refresh_shouldPreserveSession_whenTokenIsRotated() {
    stubValidRefreshToken();
    stubReplacementToken();

    when(refreshTokenRepository.rotate(
        eq(hash(REFRESH_TOKEN)),
        any(RefreshToken.class),
        any(Instant.class)
    )).thenReturn(RotationResult.ROTATED);

    authService.refresh(REFRESH_TOKEN);

    ArgumentCaptor<RefreshToken> captor =
        ArgumentCaptor.forClass(RefreshToken.class);

    verify(refreshTokenRepository).rotate(
        eq(hash(REFRESH_TOKEN)),
        captor.capture(),
        any(Instant.class)
    );

    RefreshToken replacement = captor.getValue();

    assertEquals(sessionId, replacement.getSessionId());
    assertEquals(userId, replacement.getUserId());
    assertEquals(hash(NEW_REFRESH_TOKEN), replacement.getTokenHash());
  }

  @Test
  void refresh_shouldThrowUnauthorizedException_whenTokenIsNull() {
    assertThrows(
        UnauthorizedException.class,
        () -> authService.refresh(null)
    );

    verifyNoInteractions(refreshTokenRepository);
  }

  @Test
  void refresh_shouldThrowUnauthorizedException_whenTokenIsBlank() {
    assertThrows(
        UnauthorizedException.class,
        () -> authService.refresh("   ")
    );

    verifyNoInteractions(refreshTokenRepository);
  }

  @Test
  void refresh_shouldThrowUnauthorizedException_whenTokenIsInvalid() {
    when(tokenService.isTokenValid(REFRESH_TOKEN))
        .thenReturn(false);

    assertThrows(
        UnauthorizedException.class,
        () -> authService.refresh(REFRESH_TOKEN)
    );

    verify(refreshTokenRepository, never())
        .findByTokenHash(any(String.class));
  }

  @Test
  void refresh_shouldThrowUnauthorizedException_whenTokenTypeIsIncorrect() {
    when(tokenService.isTokenValid(REFRESH_TOKEN))
        .thenReturn(true);

    when(tokenService.extractTokenType(REFRESH_TOKEN))
        .thenReturn("ACCESS");

    assertThrows(
        UnauthorizedException.class,
        () -> authService.refresh(REFRESH_TOKEN)
    );

    verify(refreshTokenRepository, never())
        .findByTokenHash(any(String.class));
  }

  @Test
  void refresh_shouldThrowUnauthorizedException_whenUserDoesNotExist() {
    when(tokenService.isTokenValid(REFRESH_TOKEN))
        .thenReturn(true);

    when(tokenService.extractTokenType(REFRESH_TOKEN))
        .thenReturn(REFRESH_TOKEN_TYPE);

    when(tokenService.extractUserId(REFRESH_TOKEN))
        .thenReturn(userId);

    when(userRepository.findById(userId))
        .thenReturn(Optional.empty());

    assertThrows(
        UnauthorizedException.class,
        () -> authService.refresh(REFRESH_TOKEN)
    );

    verify(refreshTokenRepository, never())
        .findByTokenHash(any(String.class));

    verify(refreshTokenRepository, never()).rotate(
        any(String.class),
        any(RefreshToken.class),
        any(Instant.class)
    );
  }

  @Test
  void refresh_shouldThrowUnauthorizedException_whenTokenRecordDoesNotExist() {
    when(tokenService.isTokenValid(REFRESH_TOKEN))
        .thenReturn(true);

    when(tokenService.extractTokenType(REFRESH_TOKEN))
        .thenReturn(REFRESH_TOKEN_TYPE);

    when(tokenService.extractUserId(REFRESH_TOKEN))
        .thenReturn(userId);

    when(userRepository.findById(userId))
        .thenReturn(Optional.of(user));

    when(refreshTokenRepository.findByTokenHash(hash(REFRESH_TOKEN)))
        .thenReturn(Optional.empty());

    assertThrows(
        UnauthorizedException.class,
        () -> authService.refresh(REFRESH_TOKEN)
    );

    verify(refreshTokenRepository, never()).rotate(
        any(String.class),
        any(RefreshToken.class),
        any(Instant.class)
    );
  }

  @Test
  void refresh_shouldThrowUnauthorizedException_whenTokenBelongsToAnotherUser() {
    UserId anotherUserId = new UserId(
        UUID.fromString("01912345-6789-7123-8123-123456789ac0")
    );

    RefreshToken tokenForAnotherUser = RefreshToken.create(
        new RefreshTokenId(REFRESH_UUID),
        sessionId,
        anotherUserId,
        hash(REFRESH_TOKEN),
        Instant.parse("2026-10-12T12:00:00Z"),
        EXPIRATION
    );

    when(tokenService.isTokenValid(REFRESH_TOKEN))
        .thenReturn(true);

    when(tokenService.extractTokenType(REFRESH_TOKEN))
        .thenReturn(REFRESH_TOKEN_TYPE);

    when(tokenService.extractUserId(REFRESH_TOKEN))
        .thenReturn(userId);

    when(userRepository.findById(userId))
        .thenReturn(Optional.of(user));

    when(refreshTokenRepository.findByTokenHash(hash(REFRESH_TOKEN)))
        .thenReturn(Optional.of(tokenForAnotherUser));

    assertThrows(
        UnauthorizedException.class,
        () -> authService.refresh(REFRESH_TOKEN)
    );

    verify(refreshTokenRepository, never()).rotate(
        any(String.class),
        any(RefreshToken.class),
        any(Instant.class)
    );
  }

//  @Test
//  void refresh_shouldThrowUnauthorizedException_whenRotationDetectsReuse() {
//    stubValidRefreshToken();
//
//    when(refreshTokenRepository.rotate(
//        eq(hash(REFRESH_TOKEN)),
//        any(RefreshToken.class),
//        any(Instant.class)
//    )).thenReturn(RotationResult.REUSE_DETECTED);
//
//    assertThrows(
//        UnauthorizedException.class,
//        () -> authService.refresh(REFRESH_TOKEN)
//    );
//  }

  @Test
  void refresh_shouldThrowUnauthorizedException_whenTokenIsExpiredInRepository() {
    stubValidRefreshToken();
    stubReplacementToken();

    when(refreshTokenRepository.rotate(
        eq(hash(REFRESH_TOKEN)),
        any(RefreshToken.class),
        any(Instant.class)
    )).thenReturn(RotationResult.EXPIRED);

    assertThrows(
        UnauthorizedException.class,
        () -> authService.refresh(REFRESH_TOKEN)
    );

    verify(tokenService, never()).generateAccessToken(any(User.class));
  }

  @Test
  void logout_shouldRevokeSession_whenRefreshTokenIsValidAndStored() {
    when(tokenService.isTokenValid(REFRESH_TOKEN))
        .thenReturn(true);

    when(tokenService.extractTokenType(REFRESH_TOKEN))
        .thenReturn(REFRESH_TOKEN_TYPE);

    when(refreshTokenRepository.findByTokenHash(hash(REFRESH_TOKEN)))
        .thenReturn(Optional.of(currentRefreshToken));

    authService.logout(REFRESH_TOKEN);

    verify(refreshTokenRepository).revokeSession(
        eq(sessionId),
        any(Instant.class)
    );
  }

  @Test
  void logout_shouldDoNothing_whenTokenIsNull() {
    authService.logout(null);

    verifyNoInteractions(tokenService);
    verifyNoInteractions(refreshTokenRepository);
  }

  @Test
  void logout_shouldDoNothing_whenTokenIsBlank() {
    authService.logout("   ");

    verifyNoInteractions(tokenService);
    verifyNoInteractions(refreshTokenRepository);
  }

  @Test
  void logout_shouldDoNothing_whenTokenIsInvalid() {
    when(tokenService.isTokenValid(REFRESH_TOKEN))
        .thenReturn(false);

    authService.logout(REFRESH_TOKEN);

    verify(refreshTokenRepository, never())
        .findByTokenHash(any(String.class));
  }

  @Test
  void logout_shouldDoNothing_whenTokenTypeIsIncorrect() {
    when(tokenService.isTokenValid(REFRESH_TOKEN))
        .thenReturn(true);

    when(tokenService.extractTokenType(REFRESH_TOKEN))
        .thenReturn("ACCESS");

    authService.logout(REFRESH_TOKEN);

    verify(refreshTokenRepository, never())
        .findByTokenHash(any(String.class));
  }

  @Test
  void logout_shouldDoNothing_whenTokenRecordDoesNotExist() {
    when(tokenService.isTokenValid(REFRESH_TOKEN))
        .thenReturn(true);

    when(tokenService.extractTokenType(REFRESH_TOKEN))
        .thenReturn(REFRESH_TOKEN_TYPE);

    when(refreshTokenRepository.findByTokenHash(hash(REFRESH_TOKEN)))
        .thenReturn(Optional.empty());

    authService.logout(REFRESH_TOKEN);

    verify(refreshTokenRepository, never())
        .revokeSession(any(SessionId.class), any(Instant.class));
  }

  @Test
  void logoutAll_shouldRevokeAllActiveTokensForUser() {
    authService.logoutAll(userId);

    verify(refreshTokenRepository).revokeAllActiveByUserId(
        eq(userId),
        any(Instant.class)
    );
  }

  @Test
  void logoutAll_shouldThrowIllegalArgumentException_whenUserIdIsNull() {
    assertThrows(
        IllegalArgumentException.class,
        () -> authService.logoutAll(null)
    );

    verifyNoInteractions(refreshTokenRepository);
  }

  private void stubValidRefreshToken() {
    when(tokenService.isTokenValid(REFRESH_TOKEN))
        .thenReturn(true);

    when(tokenService.extractTokenType(REFRESH_TOKEN))
        .thenReturn(REFRESH_TOKEN_TYPE);

    when(tokenService.extractUserId(REFRESH_TOKEN))
        .thenReturn(userId);

    when(userRepository.findById(userId))
        .thenReturn(Optional.of(user));

    when(refreshTokenRepository.findByTokenHash(hash(REFRESH_TOKEN)))
        .thenReturn(Optional.of(currentRefreshToken));
  }

  private void stubReplacementToken() {
    when(tokenService.generateRefreshToken(user))
        .thenReturn(NEW_REFRESH_TOKEN);

    when(tokenService.extractExpiration(NEW_REFRESH_TOKEN))
        .thenReturn(EXPIRATION);

    when(idGenerator.generate())
        .thenReturn(REPLACEMENT_UUID);

//    when(tokenService.generateAccessToken(user))
//        .thenReturn(NEW_ACCESS_TOKEN);
  }

  private void stubGeneratedTokens(UUID refreshUuid, UUID sessionUuid) {
    when(tokenService.generateAccessToken(user))
        .thenReturn(ACCESS_TOKEN);

    when(tokenService.generateRefreshToken(user))
        .thenReturn(REFRESH_TOKEN);

    when(tokenService.extractExpiration(REFRESH_TOKEN))
        .thenReturn(EXPIRATION);

    when(idGenerator.generate())
        .thenReturn(refreshUuid, sessionUuid);
  }

  private static String hash(String token) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");

      byte[] hash = digest.digest(
          token.getBytes(StandardCharsets.UTF_8)
      );

      return HexFormat.of().formatHex(hash);
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException(exception);
    }
  }
}