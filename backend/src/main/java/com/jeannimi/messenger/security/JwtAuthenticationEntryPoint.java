package com.jeannimi.messenger.security;

import com.jeannimi.messenger.exception_handling.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

  private final ObjectMapper objectMapper;

  @Override
  public void commence(
      @NonNull HttpServletRequest request, HttpServletResponse response, AuthenticationException ex)
      throws IOException {

    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType("application/json;charset=UTF-8");

    ErrorResponse error =
        new ErrorResponse(
            ex.getMessage(), HttpServletResponse.SC_UNAUTHORIZED, LocalDateTime.now());

    objectMapper.writeValue(response.getOutputStream(), error);
  }
}
