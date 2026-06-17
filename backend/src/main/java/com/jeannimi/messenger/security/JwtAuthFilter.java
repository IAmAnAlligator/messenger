package com.jeannimi.messenger.security;

import com.jeannimi.messenger.dto.CustomUserDetails;
import com.jeannimi.messenger.exception_handling.JwtAuthenticationException;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
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

    if (header == null || !header.startsWith("Bearer ")) {
      filterChain.doFilter(request, response);
      return;
    }

    String token = header.substring(7);

    try {
      var claims = jwtService.extractClaims(token);

      String tokenType = claims.get(JwtService.CLAIM_TOKEN_TYPE, String.class);
      if (!JwtService.TOKEN_TYPE_ACCESS.equals(tokenType)) {
        throw new JwtAuthenticationException("Invalid token type");
      }

      Long userId = Long.valueOf(claims.getSubject());
      String role = claims.get(JwtService.CLAIM_ROLE, String.class);

      if (role == null || role.isBlank()) {
        throw new JwtAuthenticationException("Invalid token");
      }

      CustomUserDetails user = new CustomUserDetails(userId, role);

      UsernamePasswordAuthenticationToken auth =
          new UsernamePasswordAuthenticationToken(
              user, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));

      auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

      SecurityContextHolder.getContext().setAuthentication(auth);

      filterChain.doFilter(request, response);

    } catch (ExpiredJwtException e) {
      SecurityContextHolder.clearContext();
      throw new JwtAuthenticationException("Token expired", e);

    } catch (JwtException e) {
      SecurityContextHolder.clearContext();
      throw new JwtAuthenticationException("Invalid token", e);

    } catch (Exception e) {
      SecurityContextHolder.clearContext();
      throw new JwtAuthenticationException("Authentication error", e);
    }
  }
}
