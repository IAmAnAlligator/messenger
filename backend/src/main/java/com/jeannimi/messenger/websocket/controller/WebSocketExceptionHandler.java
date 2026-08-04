package com.jeannimi.messenger.websocket.controller;

import com.jeannimi.messenger.websocket.dto.WebSocketErrorResponse;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.MethodArgumentNotValidException;

@Controller
public class WebSocketExceptionHandler {

  @MessageExceptionHandler(MethodArgumentNotValidException.class)
  @SendToUser("/queue/errors")
  public WebSocketErrorResponse handle(MethodArgumentNotValidException ex) {

    String message =
        ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .orElse("Validation error");

    return new WebSocketErrorResponse(message);
  }
}
