package com.jeannimi.messenger.common.pagination.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Constraint(validatedBy = CursorPageRequestValidator.class)
public @interface ValidCursorPageRequest {

  String message() default "cursorTime and cursorId must be provided together";

  Class<?>[] groups() default {};

  Class<? extends Payload>[] payload() default {};
}
