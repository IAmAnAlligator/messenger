package com.jeannimi.messenger.adapter.in.websocket.controller;

import com.jeannimi.messenger.adapter.in.security.websocket.WsUserPrincipal;
import com.jeannimi.messenger.adapter.in.websocket.dto.PresenceUpdateRequest;
import com.jeannimi.messenger.application.chat.service.ChatService;
import com.jeannimi.messenger.application.presence.service.UserPresenceService;
import com.jeannimi.messenger.domain.chat.ChatId;
import com.jeannimi.messenger.domain.user.UserId;
import java.security.Principal;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Slf4j
@Controller
@RequiredArgsConstructor
public class PresenceController {

  private final UserPresenceService userPresenceService;
  private final ChatService chatService;

  @MessageMapping("/presence")
  public void updatePresence(
      @Payload PresenceUpdateRequest request,
      Principal principal,
      @Header(SimpMessageHeaderAccessor.SESSION_ID_HEADER) String sessionId
  ) {
    UserId userId = extractUserId(principal);

    if (sessionId == null || sessionId.isBlank()) {
      throw new IllegalArgumentException("WebSocket session ID is required");
    }

    if (request == null) {
      throw new IllegalArgumentException("Presence update is required");
    }

    ChatId chatId = resolveChatId(request);

    if (request.visible() && !chatService.isParticipant(chatId, userId)) {
      throw new AuthenticationCredentialsNotFoundException(
          "User is not a participant of the requested chat"
      );
    }

    userPresenceService.updatePresence(
        sessionId,
        userId,
        chatId,
        request.visible()
    );

    log.debug(
        "Presence updated: userId={}, sessionId={}, chatId={}, visible={}",
        userId,
        sessionId,
        chatId,
        request.visible()
    );
  }

  private UserId extractUserId(Principal principal) {
    if (!(principal instanceof Authentication authentication)) {
      throw new AuthenticationCredentialsNotFoundException(
          "WebSocket authentication is required"
      );
    }

    if (!(authentication.getPrincipal() instanceof WsUserPrincipal wsPrincipal)) {
      throw new AuthenticationCredentialsNotFoundException(
          "Invalid WebSocket principal"
      );
    }

    return wsPrincipal.userId();
  }

  private ChatId resolveChatId(PresenceUpdateRequest request) {
    if (!request.visible()) {
      return null;
    }

    if (request.chatId() == null || request.chatId().isBlank()) {
      throw new IllegalArgumentException(
          "Chat ID is required when the chat is visible"
      );
    }

    try {
      return new ChatId(UUID.fromString(request.chatId()));
    } catch (IllegalArgumentException exception) {
      throw new IllegalArgumentException("Invalid chat ID", exception);
    }
  }
}