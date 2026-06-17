package com.jeannimi.messenger.websocket.interceptor;

import com.jeannimi.messenger.security.JwtService;
import com.jeannimi.messenger.service.ChatService;
import com.jeannimi.messenger.websocket.security.WsUserPrincipal;
import java.security.Principal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

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

    if (StompCommand.CONNECT.equals(command)) {
      handleConnect(accessor);
    }

    if (StompCommand.SUBSCRIBE.equals(command)) {
      handleSubscribe(accessor);
    }

    return message;
  }

  private void handleConnect(StompHeaderAccessor accessor) {

    String token = extractToken(accessor);

    Long userId = jwtService.extractUserId(token);
    String username = jwtService.extractUsername(token);

    validateJwtData(userId, username);

    WsUserPrincipal principal = new WsUserPrincipal(userId, username);

    UsernamePasswordAuthenticationToken authentication =
        new UsernamePasswordAuthenticationToken(principal, null, List.of());

    accessor.setUser(authentication);
  }

  private void handleSubscribe(StompHeaderAccessor accessor) {

    WsUserPrincipal principal = extractPrincipal(accessor);

    Long userId = principal.userId();

    String destination = accessor.getDestination();

    validateDestination(destination);

    Long chatId = extractChatId(destination);

    boolean isMember = chatService.isParticipant(chatId, userId);

    if (!isMember) {
      throw new IllegalArgumentException("Access denied to chat " + chatId);
    }
  }

  private String extractToken(StompHeaderAccessor accessor) {

    String authHeader = accessor.getFirstNativeHeader("Authorization");

    if (authHeader == null || authHeader.isBlank()) {
      throw new IllegalArgumentException("Missing Authorization header");
    }

    if (!authHeader.startsWith("Bearer ")) {
      throw new IllegalArgumentException("Invalid Authorization header");
    }

    String token = authHeader.substring(7);

    if (token.isBlank()) {
      throw new IllegalArgumentException("JWT token is empty");
    }

    return token;
  }

  private void validateJwtData(Long userId, String username) {

    if (userId == null) {
      throw new IllegalArgumentException("Invalid JWT: userId is missing");
    }

    if (username == null || username.isBlank()) {
      throw new IllegalArgumentException("Invalid JWT: username is missing");
    }
  }

  private WsUserPrincipal extractPrincipal(StompHeaderAccessor accessor) {

    Principal user = accessor.getUser();

    if (user == null) {
      throw new IllegalArgumentException("Unauthorized");
    }

    if (!(user instanceof Authentication authentication)) {
      throw new IllegalArgumentException("Invalid authentication object");
    }

    Object principalObj = authentication.getPrincipal();

    if (!(principalObj instanceof WsUserPrincipal principal)) {
      throw new IllegalArgumentException("Invalid principal");
    }

    if (principal.userId() == null) {
      throw new IllegalArgumentException("User ID is missing");
    }

    return principal;
  }

  private void validateDestination(String destination) {

    if (destination == null || destination.isBlank()) {
      throw new IllegalArgumentException("Destination is missing");
    }
  }

  private Long extractChatId(String destination) {

    validateDestination(destination);

    String[] parts = destination.split("/");

    if (parts.length == 0) {
      throw new IllegalArgumentException("Invalid destination format");
    }

    String chatIdPart = parts[parts.length - 1];

    if (chatIdPart.isBlank()) {
      throw new IllegalArgumentException("Chat ID is missing");
    }

    try {
      return Long.parseLong(chatIdPart);
    } catch (NumberFormatException e) {
      throw new IllegalArgumentException("Invalid chat ID: " + chatIdPart);
    }
  }
}
