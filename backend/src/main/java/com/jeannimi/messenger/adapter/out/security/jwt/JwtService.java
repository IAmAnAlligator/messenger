package com.jeannimi.messenger.adapter.out.security.jwt;

import static com.jeannimi.messenger.application.auth.AuthTokenConstants.ACCESS_TOKEN_TYPE;
import static com.jeannimi.messenger.application.auth.AuthTokenConstants.REFRESH_TOKEN_TYPE;

import com.jeannimi.messenger.application.port.out.TokenServicePort;
import com.jeannimi.messenger.domain.user.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class JwtService implements TokenServicePort {

  private static final String CLAIM_ROLE = "role";
  private static final String CLAIM_TOKEN_TYPE = "token_type";

  private final SecretKey key;

  @Value("${security.access-expiration}")
  private long jwtAccessExpiration;

  @Value("${security.refresh-expiration}")
  private long jwtRefreshExpiration;

  public JwtService(@Value("${security.secret}") String secret) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  @Override
  public String generateAccessToken(User user) {
    return Jwts.builder()
        .setSubject(String.valueOf(user.getId()))
        .claim(CLAIM_ROLE, user.getRole().name())
        .claim(CLAIM_TOKEN_TYPE, ACCESS_TOKEN_TYPE)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + jwtAccessExpiration))
        .signWith(key)
        .compact();
  }

  @Override
  public String generateRefreshToken(User user) {
    return Jwts.builder()
        .setSubject(String.valueOf(user.getId()))
        .claim(CLAIM_TOKEN_TYPE, REFRESH_TOKEN_TYPE)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + jwtRefreshExpiration))
        .signWith(key)
        .compact();
  }

  @Override
  public Long extractUserId(String token) {
    return Long.valueOf(extractClaims(token).getSubject());
  }

  /**
   * Used by the security adapter to extract the user's role from an access token.
   */
  public String extractRole(String token) {
    return extractClaims(token).get(CLAIM_ROLE, String.class);
  }

  @Override
  public String extractTokenType(String token) {
    return extractClaims(token).get(CLAIM_TOKEN_TYPE, String.class);
  }

  private Claims extractClaims(String token) {
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getBody();
  }

  @Override
  public boolean isTokenValid(String token) {
    try {
      extractClaims(token);
      return true;
    } catch (ExpiredJwtException e) {
      log.warn("Ошибка JWT, токен истёк: {}", e.getMessage());
      return false;
    } catch (JwtException e) {
      log.warn(
          "Ошибка JWT, неверная подпись, повреждённый токен и т.д.: {}",
          e.getMessage());
      return false;
    } catch (IllegalArgumentException e) {
      log.warn(
          "Ошибка JWT, null или пустая строка: {}",
          e.getMessage());
      return false;
    }
  }
}