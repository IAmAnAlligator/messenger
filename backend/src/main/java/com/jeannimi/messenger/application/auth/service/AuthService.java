package com.jeannimi.messenger.application.auth.service;

import static com.jeannimi.messenger.application.auth.AuthTokenConstants.REFRESH_TOKEN_TYPE;
import static com.jeannimi.messenger.domain.user.Role.USER;

import com.jeannimi.messenger.application.auth.dto.AuthResult;
import com.jeannimi.messenger.application.exception.ConflictException;
import com.jeannimi.messenger.application.exception.UnauthorizedException;
import com.jeannimi.messenger.application.port.out.PasswordHashPort;
import com.jeannimi.messenger.application.port.out.TokenServicePort;
import com.jeannimi.messenger.application.port.out.UserRepositoryPort;
import com.jeannimi.messenger.domain.user.Email;
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

  public AuthResult login(
      String emailValue,
      String password) {

    Email email = new Email(emailValue);

    User user =
        userRepository
            .findByEmail(email.getValue())
            .orElseThrow(
                () -> new UnauthorizedException("Invalid credentials"));

    if (!passwordHashPort.matches(
        password,
        user.getPasswordHash())) {

      throw new UnauthorizedException("Invalid credentials");
    }

    return generateTokens(user);
  }

  public AuthResult register(
      String usernameValue,
      String emailValue,
      String password) {

    Username username = new Username(usernameValue);
    Email email = new Email(emailValue);

    if (userRepository.existsByEmail(email.getValue())) {
      throw new ConflictException("Email already exists");
    }

    User user =
        User.create(
            username,
            email,
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
        tokenService.generateAccessToken(user), tokenService.generateRefreshToken(user));
  }
}
