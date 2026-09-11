package com.jeannimi.messenger.application.auth.service;

import static com.jeannimi.messenger.application.auth.AuthTokenConstants.REFRESH_TOKEN_TYPE;
import static com.jeannimi.messenger.domain.user.Role.USER;

import com.jeannimi.messenger.application.auth.dto.AuthResult;
import com.jeannimi.messenger.application.port.out.PasswordHashPort;
import com.jeannimi.messenger.application.port.out.TokenServicePort;
import com.jeannimi.messenger.application.port.out.UserRepositoryPort;
import com.jeannimi.messenger.common.exception_handling.ConflictException;
import com.jeannimi.messenger.common.exception_handling.UnauthorizedException;
import com.jeannimi.messenger.domain.user.User;
import com.jeannimi.messenger.domain.user.Username;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepositoryPort userRepository;
  private final PasswordHashPort passwordHashPort;
  private final TokenServicePort tokenService;

  public AuthResult login(String usernameValue, String password) {
    Username username = new Username(usernameValue);

    User user =
        userRepository
            .findByUsername_ValueIgnoreCase(username.getValue())
            .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

    if (!passwordHashPort.matches(password, user.getPasswordHash())) {
      throw new UnauthorizedException("Invalid credentials");
    }

    return generateTokens(user);
  }

  public AuthResult register(String usernameValue, String password) {
    Username username = new Username(usernameValue);

    if (userRepository.existsByUsername_ValueIgnoreCase(username.getValue())) {
      throw new ConflictException("User already exists");
    }

    User user =
        User.create(
            username,
            passwordHashPort.hash(password),
            USER);

    userRepository.save(user);

    return generateTokens(user);
  }

  public AuthResult refresh(String refreshToken) {
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

    Long userId = tokenService.extractUserId(refreshToken);

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UnauthorizedException("Invalid token"));

    return generateTokens(user);
  }

  private AuthResult generateTokens(User user) {
    return new AuthResult(
        tokenService.generateAccessToken(user),
        tokenService.generateRefreshToken(user));
  }
}