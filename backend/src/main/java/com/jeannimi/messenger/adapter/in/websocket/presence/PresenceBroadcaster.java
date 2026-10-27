package com.jeannimi.messenger.adapter.in.websocket.presence;

import com.jeannimi.messenger.adapter.in.websocket.dto.PresenceStatus;
import com.jeannimi.messenger.domain.user.UserId;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class PresenceBroadcaster {

  private final SimpMessagingTemplate messagingTemplate;

  public PresenceBroadcaster(@Lazy SimpMessagingTemplate messagingTemplate) {
    this.messagingTemplate = messagingTemplate;
  }

  public void publish(UserId userId, boolean online) {

    PresenceStatus status = new PresenceStatus(userId.value().toString(), online);

    messagingTemplate.convertAndSend("/topic/presence/user/" + userId.value(), status);
  }
}
