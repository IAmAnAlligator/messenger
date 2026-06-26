package com.jeannimi.messenger.service;

import static com.jeannimi.messenger.entity.Role.USER;

import com.jeannimi.messenger.domain.Username;
import com.jeannimi.messenger.dto.AuthResponse;
import com.jeannimi.messenger.dto.LoginRequest;
import com.jeannimi.messenger.dto.RegisterRequest;
import com.jeannimi.messenger.entity.User;
import com.jeannimi.messenger.exception_handling.ConflictException;
import com.jeannimi.messenger.exception_handling.UnauthorizedException;
import com.jeannimi.messenger.repository.UserRepository;
import com.jeannimi.messenger.security.JwtService;
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

    Username username = new Username(request.getUsername());

    User user =
        userRepository
            .findByUsername_ValueIgnoreCase(username.getValue())
            .orElseThrow(() -> new UnauthorizedException("Invalid credentials"));

    if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
      throw new UnauthorizedException("Invalid credentials");
    }

    String access = jwtService.generateAccessToken(user);
    String refresh = jwtService.generateRefreshToken(user);

    return new AuthResponse(access, refresh);
  }

  public AuthResponse register(RegisterRequest request) {

    Username username = new Username(request.getUsername());

    if (userRepository.existsByUsername_ValueIgnoreCase(username.getValue())) {
      throw new ConflictException("User already exists");
    }

    User user = User.of(username, passwordEncoder.encode(request.getPassword()), USER);

    userRepository.save(user);

    String access = jwtService.generateAccessToken(user);
    String refresh = jwtService.generateRefreshToken(user);

    return new AuthResponse(access, refresh);
  }

  public AuthResponse refresh(String refreshToken) {

    if (refreshToken == null) {
      throw new UnauthorizedException("No refresh token");
    }

    // 1. проверка валидности токена
    if (!jwtService.isTokenValid(refreshToken)) {
      throw new UnauthorizedException("Invalid or expired refresh token");
    }

    // 2. проверка типа токена
    String tokenType = jwtService.extractTokenType(refreshToken);
    if (!JwtService.TOKEN_TYPE_REFRESH.equals(tokenType)) {
      throw new UnauthorizedException("Invalid token type");
    }

    // 3. достаём пользователя
    Long userId = jwtService.extractUserId(refreshToken);

    User user =
        userRepository
            .findById(userId)
            .orElseThrow(() -> new UnauthorizedException("Invalid token"));

    // 4. (опционально) проверка статуса пользователя
    // if (!user.isActive()) throw ...

    // 5. генерируем новые токены
    String accessToken = jwtService.generateAccessToken(user);
    String newRefreshToken = jwtService.generateRefreshToken(user);

    return new AuthResponse(accessToken, newRefreshToken);
  }
}
