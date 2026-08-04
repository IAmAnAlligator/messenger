package com.jeannimi.messenger.auth.service;

import static com.jeannimi.messenger.user.entity.Role.USER;

import com.jeannimi.messenger.auth.dto.AuthResponse;
import com.jeannimi.messenger.auth.dto.LoginRequest;
import com.jeannimi.messenger.auth.dto.RegisterRequest;
import com.jeannimi.messenger.common.exception_handling.ConflictException;
import com.jeannimi.messenger.common.exception_handling.UnauthorizedException;
import com.jeannimi.messenger.security.jwt.JwtService;
import com.jeannimi.messenger.user.entity.User;
import com.jeannimi.messenger.user.entity.Username;
import com.jeannimi.messenger.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final UserRepository userRepository;
  private final JwtService jwtService;
  private final PasswordEncoder passwordEncoder;

  public AuthResponse login(LoginRequest request) {

    Username username = new Username(request.username());

    User user =
        userRepository
            .findByUsername_ValueIgnoreCase(username.getValue())
            .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

    if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
      throw new UnauthorizedException("Invalid credentials");
    }

    return generateTokens(user);
  }

  public AuthResponse register(RegisterRequest request) {

    Username username = new Username(request.username());

    if (userRepository.existsByUsername_ValueIgnoreCase(username.getValue())) {
      throw new ConflictException("User already exists");
    }

    User user = User.of(username, passwordEncoder.encode(request.password()), USER);

    userRepository.save(user);

    return generateTokens(user);
  }

  public AuthResponse refresh(String refreshToken) {

    if (refreshToken == null || refreshToken.isBlank()) {
      throw new UnauthorizedException("No refresh token");
    }

    if (!jwtService.isTokenValid(refreshToken)) {
      throw new UnauthorizedException("Invalid or expired refresh token");
    }

    String tokenType = jwtService.extractTokenType(refreshToken);
    if (!JwtService.TOKEN_TYPE_REFRESH.equals(tokenType)) {
      throw new UnauthorizedException("Invalid token type");
    }

    Long userId = jwtService.extractUserId(refreshToken);

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UnauthorizedException("Invalid token"));

    return generateTokens(user);
  }

  private AuthResponse generateTokens(User user) {
    return new AuthResponse(
        jwtService.generateAccessToken(user), jwtService.generateRefreshToken(user));
  }
}
