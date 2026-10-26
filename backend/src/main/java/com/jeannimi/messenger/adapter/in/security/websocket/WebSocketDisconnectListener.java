package com.jeannimi.messenger.adapter.in.security.websocket;

import com.jeannimi.messenger.adapter.in.websocket.presence.PresenceBroadcaster;
import com.jeannimi.messenger.application.presence.service.UserPresenceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Slf4j
@Component
@RequiredArgsConstructor
public class WebSocketDisconnectListener {

  private final UserPresenceService userPresenceService;
  private final PresenceBroadcaster presenceBroadcaster;

  @EventListener
  public void handleDisconnect(SessionDisconnectEvent event) {

    StompHeaderAccessor accessor =
        StompHeaderAccessor.wrap(event.getMessage());

    String sessionId = accessor.getSessionId();

    if (sessionId == null) {
      return;
    }

    userPresenceService.removeSession(sessionId)
        .ifPresent(userId -> {
          presenceBroadcaster.publish(userId, false);

          log.info(
              "WS USER OFFLINE userId={}",
              userId);
        });

    log.info("WS DISCONNECTED sessionId={}", sessionId);
  }
}