package com.jeannimi.messenger.websocket.controller;

import com.jeannimi.messenger.dto.MessageDto;
import com.jeannimi.messenger.dto.ReadResult;
import com.jeannimi.messenger.kafka.ChatEventProducer;
import com.jeannimi.messenger.service.MessageService;
import com.jeannimi.messenger.websocket.security.WsUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

  private final MessageService messageService;
  private final ChatEventProducer producer;

  @MessageMapping("/chat.send")
  public void sendMessage(MessageDto dto, Authentication authentication) {

    WsUserPrincipal principal = getPrincipal(authentication);

    validateSendMessage(dto);

    messageService.sendMessage(dto.getChatId(), principal.userId(), dto.getContent());
  }

  @MessageMapping("/chat.read")
  public void read(MessageDto dto, Authentication authentication) {

    WsUserPrincipal principal = getPrincipal(authentication);

    validateReadMessage(dto);

    ReadResult result = messageService.markAsRead(dto.getChatId(), dto.getId(), principal.userId());

    if (result.changed()) {
      producer.readMessage(result.message());
    }
  }

  private void validateSendMessage(MessageDto dto) {

    if (dto.getChatId() == null || dto.getContent() == null || dto.getContent().isBlank()) {

      throw new IllegalArgumentException("Invalid message DTO");
    }
  }

  private void validateReadMessage(MessageDto dto) {

    if (dto.getChatId() == null || dto.getId() == null) {

      throw new IllegalArgumentException("Invalid message DTO");
    }
  }

  private WsUserPrincipal getPrincipal(Authentication authentication) {

    if (authentication == null
        || !(authentication.getPrincipal() instanceof WsUserPrincipal principal)) {

      throw new AccessDeniedException("Unauthorized WebSocket request");
    }

    return principal;
  }
}
