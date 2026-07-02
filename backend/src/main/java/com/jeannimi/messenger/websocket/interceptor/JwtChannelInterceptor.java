package com.jeannimi.messenger.websocket.interceptor;

import com.jeannimi.messenger.security.JwtService;
import com.jeannimi.messenger.service.ChatService;
import com.jeannimi.messenger.websocket.security.WsUserPrincipal;
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

  // =========================
  // CONNECT
  // =========================
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
    String username;

    try {
      userId = jwtService.extractUserId(token);
      username = jwtService.extractUsername(token);
    } catch (Exception e) {
      log.warn("WS CONNECT expired/invalid JWT");
      accessor.setUser(null);
      return;
    }

    if (userId == null || username == null) {
      accessor.setUser(null);
      return;
    }

    WsUserPrincipal principal = new WsUserPrincipal(userId, username);

    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(principal, null, List.of());

    accessor.setUser(auth);

    log.info("WS CONNECTED userId={}", userId);
  }

  // =========================
  // SUBSCRIBE
  // =========================
  private void handleSubscribe(StompHeaderAccessor accessor) {

    WsUserPrincipal principal = extractPrincipal(accessor);
    if (principal == null) {
      log.warn("WS SUBSCRIBE without principal");
      return;
    }

    String destination = accessor.getDestination();

    if (destination == null) return;

    if ("/topic/chat.deleted".equals(destination)
        || "/topic/chat.created".equals(destination)) {
      return;
    }

    if (!destination.startsWith("/topic/chat/")) {
      return;
    }

    Long chatId = extractChatId(destination);

    boolean isMember = chatService.isParticipant(chatId, principal.userId());

    if (!isMember) {
      log.warn("WS SUBSCRIBE denied userId={} chatId={}", principal.userId(), chatId);
      return; // ❗ IMPORTANT: no throw
    }
  }

  // =========================
  // SEND (no security logic here for now)
  // =========================
  private void handleSend(StompHeaderAccessor accessor) {
    // intentionally empty or extend later
  }

  // =========================
  // PRINCIPAL
  // =========================
  private WsUserPrincipal extractPrincipal(StompHeaderAccessor accessor) {

    Principal user = accessor.getUser();

    if (!(user instanceof Authentication authentication)) {
      return null;
    }

    Object p = authentication.getPrincipal();

    if (!(p instanceof WsUserPrincipal principal)) {
      return null;
    }

    return principal;
  }

  // =========================
  // TOKEN
  // =========================
  private String extractToken(StompHeaderAccessor accessor) {

    String authHeader = accessor.getFirstNativeHeader("Authorization");

    if (authHeader == null || !authHeader.startsWith("Bearer ")) {
      throw new IllegalArgumentException("Invalid Authorization header");
    }

    return authHeader.substring(7);
  }

  // =========================
  // DESTINATION PARSING
  // =========================
  private Long extractChatId(String destination) {

    String[] parts = destination.split("/");

    try {
      return Long.parseLong(parts[parts.length - 1]);
    } catch (Exception e) {
      throw new IllegalArgumentException("Invalid chat id");
    }
  }
}