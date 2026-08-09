package com.jeannimi.messenger.common.pagination.validation;

import com.jeannimi.messenger.common.pagination.CursorPageRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class CursorPageRequestValidator
    implements ConstraintValidator<ValidCursorPageRequest, CursorPageRequest> {

  @Override
  public boolean isValid(CursorPageRequest request, ConstraintValidatorContext context) {

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

    if (request.cursorTime() == null) {

      context
          .buildConstraintViolationWithTemplate("cursorTime is required when cursorId is provided")
          .addPropertyNode("cursorTime")
          .addConstraintViolation();
    }

    if (request.cursorId() == null) {

      context
          .buildConstraintViolationWithTemplate("cursorId is required when cursorTime is provided")
          .addPropertyNode("cursorId")
          .addConstraintViolation();
    }

    return false;
  }
}
