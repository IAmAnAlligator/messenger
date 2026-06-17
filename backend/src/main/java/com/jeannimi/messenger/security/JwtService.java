package com.jeannimi.messenger.security;

import com.jeannimi.messenger.entity.User;
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
public class JwtService {

  public static final String CLAIM_ROLE = "role";
  public static final String CLAIM_TOKEN_TYPE = "token_type";
  public static final String TOKEN_TYPE_ACCESS = "ACCESS";
  public static final String TOKEN_TYPE_REFRESH = "REFRESH";

  private final SecretKey key;

  @Value("${security.access-expiration}")
  private long jwtAccessExpiration;

  @Value("${security.refresh-expiration}")
  private long jwtRefreshExpiration;

  public JwtService(@Value("${security.secret}") String secret) {
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  public String generateAccessToken(User user) {

    return Jwts.builder()
        .setSubject(String.valueOf(user.getId()))
        .claim(CLAIM_ROLE, user.getRole().name())
        .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_ACCESS)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + jwtAccessExpiration))
        .signWith(key)
        .compact();
  }

  public String generateRefreshToken(User user) {

    return Jwts.builder()
        .setSubject(String.valueOf(user.getId()))
        .claim(CLAIM_TOKEN_TYPE, TOKEN_TYPE_REFRESH)
        .setIssuedAt(new Date())
        .setExpiration(new Date(System.currentTimeMillis() + jwtRefreshExpiration))
        .signWith(key)
        .compact();
  }

  public Long extractUserId(String token) {
    return Long.valueOf(extractClaims(token).getSubject());
  }

  public String extractRole(String token) {
    return extractClaims(token).get(CLAIM_ROLE, String.class);
  }

  public String extractTokenType(String token) {
    return extractClaims(token).get(CLAIM_TOKEN_TYPE, String.class);
  }

  Claims extractClaims(String token) {

    return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getBody();
  }

  public boolean isTokenValid(String token) {
    try {
      extractClaims(token); // проверка подписи и срока действия
      return true;
    } catch (ExpiredJwtException e) {
      log.warn("Ошибка JWT, токен истёк: {}", e.getMessage());
      return false;
    } catch (JwtException e) {
      log.warn("Ошибка JWT, неверная подпись, повреждённый токен и т.д.: {}", e.getMessage());
      return false;
    } catch (IllegalArgumentException e) {
      log.warn("Ошибка JWT, null или пустая строка: {}", e.getMessage());
      return false;
    }
  }

  public String extractUsername(String token) {

    return extractClaims(token).getSubject();
  }
}
