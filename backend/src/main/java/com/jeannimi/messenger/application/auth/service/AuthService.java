package com.jeannimi.messenger.application.auth.service;

import static com.jeannimi.messenger.application.auth.AuthTokenConstants.REFRESH_TOKEN_TYPE;
import static com.jeannimi.messenger.domain.user.Role.USER;

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
import com.jeannimi.messenger.domain.user.Email;
import com.jeannimi.messenger.domain.user.Handle;
import com.jeannimi.messenger.domain.user.Password;
import com.jeannimi.messenger.domain.user.User;
import com.jeannimi.messenger.domain.user.UserId;
import com.jeannimi.messenger.domain.user.Username;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepositoryPort userRepository;
  private final PasswordHashPort passwordHashPort;
  private final TokenServicePort tokenService;
  private final HandleGeneratorPort handleGenerator;
  private final IdGenerator idGenerator;
  private final RefreshTokenRepositoryPort refreshTokenRepository;
  private final RefreshTokenRevocationService refreshTokenRevocationService;

  @Transactional
  public AuthResult login(String emailValue, String password) {

    Email email = new Email(emailValue);
    Password passwordValue = new Password(password);

    User user =
        userRepository
            .findByEmail(email.getValue())
            .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

    if (!passwordHashPort.matches(passwordValue, user.getPasswordHash())) {
      throw new UnauthorizedException("Invalid credentials");
    }

    return generateTokens(user);
  }

  @Transactional
  public AuthResult register(String usernameValue, String emailValue, String password) {
    Username username = new Username(usernameValue);
    Email email = new Email(emailValue);
    Password passwordValue = new Password(password);

    if (userRepository.existsByEmail(email.getValue())) {
      throw new ConflictException("Email already exists");
    }

    Handle handle = handleGenerator.generate();
    UserId userId = new UserId(idGenerator.generate());

    User user =
        User.create(userId, handle, username, email, passwordHashPort.hash(passwordValue), USER);

    userRepository.save(user);

    return generateTokens(user);
  }

  @Transactional
  public AuthResult refresh(String refreshToken) {
    validateRefreshToken(refreshToken);

    UserId userId = tokenService.extractUserId(refreshToken);

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UnauthorizedException("Invalid token"));

    String currentTokenHash = hashToken(refreshToken);

    RefreshToken current =
        refreshTokenRepository
            .findByTokenHash(currentTokenHash)
            .orElseThrow(() -> new UnauthorizedException("Refresh token not found"));

    if (!current.getUserId().equals(userId)) {
      throw new UnauthorizedException("Invalid refresh token");
    }

    Instant now = Instant.now();

    String newRefreshToken = tokenService.generateRefreshToken(user);
    RefreshToken replacement =
        createReplacement(userId, current.getSessionId(), newRefreshToken, now);

    rotateRefreshToken(currentTokenHash, replacement, current.getSessionId(), now);

    return new AuthResult(tokenService.generateAccessToken(user), newRefreshToken);
  }

  private void validateRefreshToken(String refreshToken) {
    if (refreshToken == null || refreshToken.isBlank()) {
      throw new UnauthorizedException("No refresh token");
    }

    if (!tokenService.isTokenValid(refreshToken)) {
      throw new UnauthorizedException("Invalid or expired refresh token");
    }

    String tokenType = tokenService.extractTokenType(refreshToken);

    if (!REFRESH_TOKEN_TYPE.equals(tokenType)) {
      throw new UnauthorizedException("Invalid token type");
    }
  }

  private RefreshToken createReplacement(
      UserId userId, SessionId sessionId, String newRefreshToken, Instant now) {
    return RefreshToken.create(
        new RefreshTokenId(idGenerator.generate()),
        sessionId,
        userId,
        hashToken(newRefreshToken),
        now,
        tokenService.extractExpiration(newRefreshToken));
  }

  private void rotateRefreshToken(
      String currentTokenHash, RefreshToken replacement, SessionId sessionId, Instant now) {
    RotationResult result = refreshTokenRepository.rotate(currentTokenHash, replacement, now);

    if (result == RotationResult.REUSE_DETECTED) {
      refreshTokenRevocationService.revokeSession(sessionId, now);

      throw new UnauthorizedException("Refresh token is invalid or has already been used");
    }

    if (result != RotationResult.ROTATED) {
      throw new UnauthorizedException("Refresh token is invalid or has already been used");
    }
  }

  private AuthResult generateTokens(User user) {

    String accessToken = tokenService.generateAccessToken(user);

    String refreshToken = tokenService.generateRefreshToken(user);

    Instant now = Instant.now();

    RefreshToken refreshTokenEntity =
        RefreshToken.create(
            new RefreshTokenId(idGenerator.generate()),
            new SessionId(idGenerator.generate()),
            user.getId(),
            hashToken(refreshToken),
            now,
            tokenService.extractExpiration(refreshToken));

    refreshTokenRepository.save(refreshTokenEntity);

    return new AuthResult(accessToken, refreshToken);
  }

  private String hashToken(String token) {

    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");

      byte[] hash = digest.digest(token.getBytes(StandardCharsets.UTF_8));

      return HexFormat.of().formatHex(hash);

    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 algorithm is unavailable", exception);
    }
  }

  @Transactional
  public void logout(String refreshToken) {

    if (refreshToken == null || refreshToken.isBlank()) {
      return;
    }

    if (!tokenService.isTokenValid(refreshToken)) {
      return;
    }

    String tokenType = tokenService.extractTokenType(refreshToken);

    if (!REFRESH_TOKEN_TYPE.equals(tokenType)) {
      return;
    }

    String tokenHash = hashToken(refreshToken);

    RefreshToken refreshTokenEntity =
        refreshTokenRepository.findByTokenHash(tokenHash).orElse(null);

    if (refreshTokenEntity == null) {
      return;
    }

    Instant now = Instant.now();

    refreshTokenRepository.revokeSession(refreshTokenEntity.getSessionId(), now);
  }

  public void logoutAll(UserId userId) {

    if (userId == null) {
      throw new IllegalArgumentException("User ID must not be null");
    }

    refreshTokenRepository.revokeAllActiveByUserId(userId, Instant.now());
  }
}
