package com.jeannimi.messenger.adapter.in.security.jwt;

import com.jeannimi.messenger.adapter.in.security.CustomUserDetails;
import com.jeannimi.messenger.application.port.out.TokenServicePort;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

  private static final String BEARER_PREFIX = "Bearer ";
  private static final String ACCESS_TOKEN_TYPE = "ACCESS";

  private final TokenServicePort tokenService;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    if (!hasAuthentication()) {
      authenticateIfTokenPresent(request);
    }

    filterChain.doFilter(request, response);
  }

  private boolean hasAuthentication() {
    return SecurityContextHolder.getContext().getAuthentication() != null;
  }

  private void authenticateIfTokenPresent(HttpServletRequest request) {

    String header = request.getHeader("Authorization");

    if (hasBearerToken(header)) {
      String token = extractToken(header);

      try {
        authenticate(token, request);
      } catch (JwtAuthenticationException e) {
        clearContext();
        throw e;
      }
    }
  }

  private boolean hasBearerToken(String header) {

    return header != null && header.startsWith(BEARER_PREFIX);
  }

  private String extractToken(String header) {

    String token = header.substring(BEARER_PREFIX.length()).trim();

    if (token.isBlank()) {
      throw new JwtAuthenticationException("Invalid token");
    }

    return token;
  }

  private void authenticate(String token, HttpServletRequest request) {

    validateToken(token);

    String tokenType = tokenService.extractTokenType(token);

    validateAccessToken(tokenType);

    Long userId = tokenService.extractUserId(token);
    String role = tokenService.extractRole(token);

    validateRole(role);

    CustomUserDetails user = new CustomUserDetails(userId, role);

    UsernamePasswordAuthenticationToken authentication =
        createAuthentication(user, role);

    authentication.setDetails(
        new WebAuthenticationDetailsSource().buildDetails(request));

    SecurityContextHolder.getContext().setAuthentication(authentication);
  }

  private void validateToken(String token) {

    if (!tokenService.isTokenValid(token)) {
      throw new JwtAuthenticationException("Invalid or expired token");
    }
  }

  private void validateAccessToken(String tokenType) {

    if (!ACCESS_TOKEN_TYPE.equals(tokenType)) {
      throw new JwtAuthenticationException("Invalid token type");
    }
  }

  private void validateRole(String role) {

    if (role == null || role.isBlank()) {
      throw new JwtAuthenticationException("Invalid token");
    }
  }

  private UsernamePasswordAuthenticationToken createAuthentication(
      CustomUserDetails user,
      String role) {

    return new UsernamePasswordAuthenticationToken(
        user,
        null,
        List.of(new SimpleGrantedAuthority("ROLE_" + role)));
  }

  private void clearContext() {
    SecurityContextHolder.clearContext();
  }
}