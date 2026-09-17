package com.jeannimi.messenger.adapter.in.security.websocket;

import static com.jeannimi.messenger.application.auth.AuthTokenConstants.ACCESS_TOKEN_TYPE;

import com.jeannimi.messenger.application.port.out.TokenServicePort;
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
import org.jspecify.annotations.NonNull;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtChannelInterceptor implements ChannelInterceptor {

  private static final String BEARER_PREFIX = "Bearer ";

  private final TokenServicePort tokenService;
  private final ChatService chatService;

  @Override
  public Message<?> preSend(@NonNull Message<?> message, @NonNull MessageChannel channel) {

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

      case SUBSCRIBE -> {
        if (!handleSubscribe(accessor)) {
          return null;
        }
      }

      default -> {
        // ignore
      }
    }

    return message;
  }

  private String getToken(StompHeaderAccessor accessor) {

    try {
      return extractToken(accessor);
    } catch (IllegalArgumentException e) {
      log.warn("WS CONNECT missing/invalid token");
      throw e;
    }
  }

  private Long getUserId(String token) {

    if (!tokenService.isTokenValid(token)) {
      throw new IllegalArgumentException("Invalid or expired JWT");
    }

    String tokenType = tokenService.extractTokenType(token);

    if (!ACCESS_TOKEN_TYPE.equals(tokenType)) {
      throw new IllegalArgumentException("Invalid token type");
    }

    Long userId = tokenService.extractUserId(token);

    if (userId == null) {
      throw new IllegalArgumentException("JWT does not contain user id");
    }

    return userId;
  }

  private void handleConnect(StompHeaderAccessor accessor) {

    String token = getToken(accessor);
    Long userId = getUserId(token);

    WsUserPrincipal principal = new WsUserPrincipal(userId);

    UsernamePasswordAuthenticationToken auth =
        new UsernamePasswordAuthenticationToken(
            principal,
            null,
            List.of());

    accessor.setUser(auth);

    log.info("WS CONNECTED userId={}", userId);
  }

  private boolean handleSubscribe(StompHeaderAccessor accessor) {

    WsUserPrincipal principal = extractPrincipal(accessor);
    String destination = accessor.getDestination();

    log.info(
        "WS SUBSCRIBE destination={} principal={}",
        destination,
        principal);

    if (principal == null) {
      log.warn("WS SUBSCRIBE without principal");
      return false;
    }

    if (destination == null) {
      return false;
    }

    if (isPublicDestination(destination)) {
      return true;
    }

    if (!isChatDestination(destination)) {
      return true;
    }

    return isChatSubscriptionAllowed(destination, principal);
  }

  private boolean isPublicDestination(String destination) {

    return "/topic/chat.deleted".equals(destination)
        || "/topic/chat.created".equals(destination);
  }

  private boolean isChatDestination(String destination) {
    return destination.startsWith("/topic/chat/");
  }

  private boolean isChatSubscriptionAllowed(
      String destination,
      WsUserPrincipal principal) {

    Long chatId = extractChatId(destination);

    boolean isMember =
        chatService.isParticipant(
            chatId,
            principal.userId());

    log.info(
        "WS SUBSCRIBE permission userId={} chatId={} isMember={}",
        principal.userId(),
        chatId,
        isMember);

    if (!isMember) {
      log.warn(
          "WS SUBSCRIBE denied userId={} chatId={}",
          principal.userId(),
          chatId);

      return false;
    }

    log.info(
        "WS SUBSCRIBE allowed userId={} chatId={}",
        principal.userId(),
        chatId);

    return true;
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

    String token = authHeader.substring(BEARER_PREFIX.length());

    if (token.isBlank()) {
      throw new IllegalArgumentException(
          "Invalid Authorization header");
    }

    return token;
  }

  private Long extractChatId(String destination) {

    String[] parts = destination.split("/");

    if (parts.length != 4) {
      throw new IllegalArgumentException("Invalid chat destination");
    }

    try {
      return Long.parseLong(parts[3]);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid chat id");
    }
  }
}