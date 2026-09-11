package com.jeannimi.messenger.adapter.in.security.jwt;

import com.jeannimi.messenger.adapter.in.security.CustomUserDetails;
import com.jeannimi.messenger.adapter.out.security.jwt.JwtService;
import com.jeannimi.messenger.common.exception_handling.JwtAuthenticationException;
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

  private final JwtService jwtService;

  @Override
  protected void doFilterInternal(
      @NonNull HttpServletRequest request,
      @NonNull HttpServletResponse response,
      @NonNull FilterChain filterChain)
      throws ServletException, IOException {

    if (SecurityContextHolder.getContext().getAuthentication() != null) {
      filterChain.doFilter(request, response);
      return;
    }

    String header = request.getHeader("Authorization");

    if (header == null || !header.startsWith(BEARER_PREFIX)) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = header.substring(BEARER_PREFIX.length());

    try {
      if (!jwtService.isTokenValid(token)) {
        throw new JwtAuthenticationException("Invalid or expired token");
      }

      String tokenType = jwtService.extractTokenType(token);

      if (!ACCESS_TOKEN_TYPE.equals(tokenType)) {
        throw new JwtAuthenticationException("Invalid token type");
      }

      Long userId = jwtService.extractUserId(token);
      String role = jwtService.extractRole(token);

      if (role == null || role.isBlank()) {
        throw new JwtAuthenticationException("Invalid token");
      }

      CustomUserDetails user = new CustomUserDetails(userId, role);

      UsernamePasswordAuthenticationToken auth =
          new UsernamePasswordAuthenticationToken(
              user,
              null,
              List.of(new SimpleGrantedAuthority("ROLE_" + role)));

      auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

      SecurityContextHolder.getContext().setAuthentication(auth);

      filterChain.doFilter(request, response);

    } catch (JwtAuthenticationException e) {
      SecurityContextHolder.clearContext();
      throw e;

    } catch (Exception e) {
      SecurityContextHolder.clearContext();
      throw new JwtAuthenticationException("Authentication error", e);
    }
  }
}