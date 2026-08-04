package com.jeannimi.messenger.validation;

import com.jeannimi.messenger.chat.dto.ChatPageRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class ChatPageRequestValidator
    implements ConstraintValidator<ValidChatPageRequest, ChatPageRequest> {

  @Override
  public boolean isValid(ChatPageRequest request, ConstraintValidatorContext context) {

    if (request == null) {
      return true;
    }

    boolean valid =
        (request.cursorTime() == null && request.cursorId() == null)
            || (request.cursorTime() != null && request.cursorId() != null);

    if (valid) {
      return true;
    }

    context.disableDefaultConstraintViolation();

    context
        .buildConstraintViolationWithTemplate("cursorTime is required when cursorId is provided")
        .addPropertyNode("cursorTime")
        .addConstraintViolation();

    context
        .buildConstraintViolationWithTemplate("cursorId is required when cursorTime is provided")
        .addPropertyNode("cursorId")
        .addConstraintViolation();

    return false;
  }
}
