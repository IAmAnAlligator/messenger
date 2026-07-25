package com.jeannimi.messenger.websocket.controller;

import com.jeannimi.messenger.message.dto.DeleteMessageDto;
import com.jeannimi.messenger.message.dto.MessageDto;
import com.jeannimi.messenger.message.service.MessageService;
import com.jeannimi.messenger.security.websocker.WsUserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

  private final MessageService messageService;

  @MessageMapping("/chat.send")
  public void sendMessage(MessageDto dto, Authentication authentication) {

    WsUserPrincipal principal = getPrincipal(authentication);

    validateSendMessage(dto);

    messageService.sendMessage(dto.chatId(), principal.userId(), dto.content());
  }

  @MessageMapping("/chat.read")
  public void read(MessageDto dto, Authentication authentication) {

    WsUserPrincipal principal = getPrincipal(authentication);

    validateReadMessage(dto);

    messageService.markAsRead(dto.chatId(), dto.id(), principal.userId());
  }

  @MessageMapping("/chat.delete")
  public void delete(DeleteMessageDto dto, Authentication authentication) {

    WsUserPrincipal principal = getPrincipal(authentication);

    validateDeleteMessage(dto);

    messageService.deleteMessage(dto.chatId(), dto.id(), principal.userId());

  }

  private void validateDeleteMessage(DeleteMessageDto dto) {

    if (dto.chatId() == null || dto.id() == null) {

      throw new IllegalArgumentException("Invalid delete DTO");
    }
  }

  private void validateSendMessage(MessageDto dto) {

    if (dto.chatId() == null || dto.content() == null || dto.content().isBlank()) {

      throw new IllegalArgumentException("Invalid message DTO");
    }
  }

  private void validateReadMessage(MessageDto dto) {

    if (dto.chatId() == null || dto.id() == null) {

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
