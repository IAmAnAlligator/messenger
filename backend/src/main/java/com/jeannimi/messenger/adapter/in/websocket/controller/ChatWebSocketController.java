package com.jeannimi.messenger.adapter.in.websocket.controller;

import com.jeannimi.messenger.adapter.in.security.websocket.WsUserPrincipal;
import com.jeannimi.messenger.adapter.in.websocket.dto.DeleteMessageCommand;
import com.jeannimi.messenger.adapter.in.websocket.dto.ReadMessageCommand;
import com.jeannimi.messenger.adapter.in.websocket.dto.SendMessageCommand;
import com.jeannimi.messenger.application.exception.ForbiddenException;
import com.jeannimi.messenger.application.message.service.MessageService;
import com.jeannimi.messenger.domain.chat.ChatId;
import com.jeannimi.messenger.domain.message.MessageId;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

  private final MessageService messageService;

  @MessageMapping("/chat.send")
  public void sendMessage(@Valid SendMessageCommand command, Authentication authentication) {

    WsUserPrincipal principal = getPrincipal(authentication);

    ChatId chatId = new ChatId(command.chatId());

    messageService.sendMessage(chatId, principal.userId(), command.content());
  }

  @MessageMapping("/chat.read")
  public void read(@Valid ReadMessageCommand command, Authentication authentication) {

    WsUserPrincipal principal = getPrincipal(authentication);

    ChatId chatId = new ChatId(command.chatId());
    MessageId messageId = new MessageId(command.messageId());

    messageService.markAsRead(chatId, messageId, principal.userId());
  }

  @MessageMapping("/chat.delete")
  public void delete(@Valid DeleteMessageCommand command, Authentication authentication) {

    WsUserPrincipal principal = getPrincipal(authentication);

    ChatId chatId = new ChatId(command.chatId());
    MessageId messageId = new MessageId(command.messageId());

    messageService.deleteMessage(chatId, messageId, principal.userId());
  }

  private WsUserPrincipal getPrincipal(Authentication authentication) {

    if (authentication == null
        || !(authentication.getPrincipal() instanceof WsUserPrincipal principal)) {

      throw new ForbiddenException("Unauthorized WebSocket request");
    }

    return principal;
  }
}
