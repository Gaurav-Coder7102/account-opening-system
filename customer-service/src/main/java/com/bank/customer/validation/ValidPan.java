package com.bank.customer.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validates Indian PAN number format: 5 uppercase letters, 4 digits, 1 uppercase letter
 * Example: ABCDE1234F
 */
@Documented
@Constraint(validatedBy = PanValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPan {
    String message() default "PAN must be in the format: 5 uppercase letters, 4 digits, 1 uppercase letter (e.g., ABCDE1234F)";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
