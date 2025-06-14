package com.fuar.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = CreateEventDateValidator.class)
@Documented
public @interface CreateEventDateValidation {
    String message() default "For new events, dates must be in the future";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
