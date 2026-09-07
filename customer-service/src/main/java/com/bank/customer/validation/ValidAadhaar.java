package com.bank.customer.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

/**
 * Validates Indian Aadhaar number: exactly 12 digits.
 * Example: 123456789012
 */
@Documented
@Constraint(validatedBy = AadhaarValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidAadhaar {
    String message() default "Aadhaar number must be exactly 12 digits";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
