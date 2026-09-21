package com.jeannimi.messenger.adapter.in.websocket.controller;

import com.jeannimi.messenger.adapter.in.websocket.dto.WebSocketErrorResponse;
import com.jeannimi.messenger.application.exception.ForbiddenException;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.simp.annotation.SendToUser;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;

@ControllerAdvice
public class WebSocketExceptionHandler {

  @MessageExceptionHandler(MethodArgumentNotValidException.class)
  @SendToUser("/queue/errors")
  public WebSocketErrorResponse handleValidation(MethodArgumentNotValidException ex) {

    String message =
        ex.getBindingResult().getFieldErrors().stream()
            .findFirst()
            .map(DefaultMessageSourceResolvable::getDefaultMessage)
            .orElse("Validation error");

    return new WebSocketErrorResponse(message);
  }

  @MessageExceptionHandler(ForbiddenException.class)
  @SendToUser("/queue/errors")
  public WebSocketErrorResponse handleForbidden(ForbiddenException ex) {

    return new WebSocketErrorResponse(ex.getMessage());
  }
}
