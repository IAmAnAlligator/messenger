package com.jeannimi.messenger.adapter.in.security.websocket;

import static com.jeannimi.messenger.application.auth.AuthTokenConstants.ACCESS_TOKEN_TYPE;

import com.jeannimi.messenger.adapter.out.security.jwt.JwtService;
import com.jeannimi.messenger.application.chat.service.ChatService;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

  private static final String BEARER_PREFIX = "Bearer ";

  private final JwtService jwtService;
  private final ChatService chatService;

  @Override
  public Message<?> preSend(Message<?> message, MessageChannel channel) {

    StompHeaderAccessor accessor =
        MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

    if (accessor == null) {
      return message;
    }

    StompCommand command = accessor.getCommand();

    if (command == null) {
      return message;
    }

    switch (command) {
      case CONNECT -> handleConnect(accessor);
      case SUBSCRIBE -> handleSubscribe(accessor);
      case SEND -> handleSend(accessor);
      default -> {
        // ignore
      }
    }

    return message;
  }

  private void handleConnect(StompHeaderAccessor accessor) {

    String token;

    try {
      token = extractToken(accessor);
    } catch (Exception e) {
      log.warn("WS CONNECT missing/invalid token");
      accessor.setUser(null);
      return;
    }

    Long userId;

    try {
      if (!jwtService.isTokenValid(token)) {
        throw new IllegalArgumentException("Invalid or expired JWT");
      }

      String tokenType = jwtService.extractTokenType(token);

      if (!ACCESS_TOKEN_TYPE.equals(tokenType)) {
        throw new IllegalArgumentException("Invalid token type");
      }

      userId = jwtService.extractUserId(token);

    } catch (Exception e) {
      log.warn("WS CONNECT expired/invalid JWT");
      accessor.setUser(null);
      return;
    }

    if (userId == null) {
      accessor.setUser(null);
      return;
    }

    WsUserPrincipal principal = new WsUserPrincipal(userId);

    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(
            principal,
            null,
            List.of());

    accessor.setUser(auth);

    log.info("WS CONNECTED userId={}", userId);
  }

  private void handleSubscribe(StompHeaderAccessor accessor) {

    WsUserPrincipal principal = extractPrincipal(accessor);

    if (principal == null) {
      log.warn("WS SUBSCRIBE without principal");
      return;
    }

    String destination = accessor.getDestination();

    if (destination == null) {
      return;
    }

    if ("/topic/chat.deleted".equals(destination)
        || "/topic/chat.created".equals(destination)) {
      return;
    }

    if (!destination.startsWith("/topic/chat/")) {
      return;
    }

    Long chatId = extractChatId(destination);

    boolean isMember =
        chatService.isParticipant(chatId, principal.userId());

    if (!isMember) {
      log.warn(
          "WS SUBSCRIBE denied userId={} chatId={}",
          principal.userId(),
          chatId);
      return;
    }
  }

  private void handleSend(StompHeaderAccessor accessor) {
    // intentionally empty or extend later
  }

  private WsUserPrincipal extractPrincipal(
      StompHeaderAccessor accessor) {

    Principal user = accessor.getUser();

    if (!(user instanceof Authentication authentication)) {
      return null;
    }

    Object principal = authentication.getPrincipal();

    if (!(principal instanceof WsUserPrincipal wsUserPrincipal)) {
      return null;
    }

    return wsUserPrincipal;
  }

  private String extractToken(StompHeaderAccessor accessor) {

    String authHeader =
        accessor.getFirstNativeHeader("Authorization");

    if (authHeader == null
        || !authHeader.startsWith(BEARER_PREFIX)) {
      throw new IllegalArgumentException(
          "Invalid Authorization header");
    }

    return authHeader.substring(BEARER_PREFIX.length());
  }

  private Long extractChatId(String destination) {

    String[] parts = destination.split("/");

    try {
      return Long.parseLong(parts[parts.length - 1]);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid chat id");
    }
  }
}