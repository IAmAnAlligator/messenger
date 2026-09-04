package com.jeannimi.messenger.websocket.controller;

import com.jeannimi.messenger.message.service.MessageService;
import com.jeannimi.messenger.security.websocker.WsUserPrincipal;
import com.jeannimi.messenger.websocket.dto.DeleteMessageCommand;
import com.jeannimi.messenger.websocket.dto.ReadMessageCommand;
import com.jeannimi.messenger.websocket.dto.SendMessageCommand;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

  // 1. Клиент → Сервер (@MessageMapping)
  //
  // Это команды пользователя. Именно их обрабатывает ChatWebSocketController.

  private final MessageService messageService;

  @MessageMapping("/chat.send")
  public void sendMessage(@Valid SendMessageCommand command, Authentication authentication) {

    WsUserPrincipal principal = getPrincipal(authentication);

    messageService.sendMessage(command.chatId(), principal.userId(), command.content());
  }

  @MessageMapping("/chat.read")
  public void read(@Valid ReadMessageCommand command, Authentication authentication) {

    WsUserPrincipal principal = getPrincipal(authentication);

    messageService.markAsRead(command.chatId(), command.messageId(), principal.userId());
  }

  @MessageMapping("/chat.delete")
  public void delete(@Valid DeleteMessageCommand command, Authentication authentication) {

    WsUserPrincipal principal = getPrincipal(authentication);

    messageService.deleteMessage(command.chatId(), command.messageId(), principal.userId());
  }

  private WsUserPrincipal getPrincipal(Authentication authentication) {

    if (authentication == null
        || !(authentication.getPrincipal() instanceof WsUserPrincipal principal)) {

      throw new AccessDeniedException("Unauthorized WebSocket request");
    }

    return principal;
  }
}
