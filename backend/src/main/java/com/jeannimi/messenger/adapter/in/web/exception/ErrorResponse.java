package com.jeannimi.messenger.adapter.in.web.exception;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ErrorResponse {

  private String message;
  private int status;
  private LocalDateTime timestamp;
}
