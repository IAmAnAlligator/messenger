package com.jeannimi.messenger.exception_handling;

import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  @ExceptionHandler(UnauthorizedException.class)
  public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex) {
    return buildResponse(ex.getMessage(), HttpStatus.UNAUTHORIZED);
  }

  // =========================
  // 400 - Bad Request
  // =========================
  @ExceptionHandler(BadRequestException.class)
  public ResponseEntity<ErrorResponse> handleBadRequest(BadRequestException ex) {
    return buildResponse(ex.getMessage(), HttpStatus.BAD_REQUEST);
  }

  // =========================
  // 403 - Forbidden
  // =========================
  @ExceptionHandler(ForbiddenException.class)
  public ResponseEntity<ErrorResponse> handleForbidden(ForbiddenException ex) {
    return buildResponse(ex.getMessage(), HttpStatus.FORBIDDEN);
  }

  // =========================
  // 404 - Not Found
  // =========================
  @ExceptionHandler(NotFoundException.class)
  public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex) {
    return buildResponse(ex.getMessage(), HttpStatus.NOT_FOUND);
  }

  // =========================
  // 409 - Conflict
  // =========================
  @ExceptionHandler(ConflictException.class)
  public ResponseEntity<ErrorResponse> handleConflict(ConflictException ex) {
    return buildResponse(ex.getMessage(), HttpStatus.CONFLICT);
  }

  // =========================
  // Validation (@Valid)
  // =========================
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {

    String message =
        ex.getBindingResult().getFieldErrors().stream()
            .map(err -> err.getField() + ": " + err.getDefaultMessage())
            .findFirst()
            .orElse("Validation error");

    return buildResponse(message, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ChatException.class)
  public ResponseEntity<ErrorResponse> handle(ChatException ex) {

    HttpStatus status = switch (ex.getError()) {

      case NOT_CHAT_MEMBER,
           ONLY_ADMIN_ALLOWED,
           ADMIN_CANNOT_LEAVE ->
          HttpStatus.FORBIDDEN;

      case USER_ALREADY_IN_CHAT,
           LAST_ADMIN_CANNOT_BE_REMOVED,
           PRIVATE_CHAT_MUST_HAVE_TWO_MEMBERS,
           PRIVATE_CHAT_MUST_HAVE_PRIVATE_KEY ->
          HttpStatus.CONFLICT;

      case MEMBER_NOT_FOUND ->
          HttpStatus.NOT_FOUND;

      default ->
          HttpStatus.BAD_REQUEST;
    };

    return ResponseEntity
        .status(status)
        .body(new ErrorResponse(ex.getMessage(), status.value(), LocalDateTime.now()));
  }

  // =========================
  // Fallback (500)
  // =========================
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ErrorResponse> handleAll(Exception ex) {

    // 👉 логировать ОБЯЗАТЕЛЬНО
    log.error("Unhandled exception", ex);

    return buildResponse("Internal server error", HttpStatus.INTERNAL_SERVER_ERROR);
  }

  // =========================
  // helper
  // =========================
  private ResponseEntity<ErrorResponse> buildResponse(String message, HttpStatus status) {
    ErrorResponse error = new ErrorResponse(message, status.value(), LocalDateTime.now());
    return new ResponseEntity<>(error, status);
  }
}
