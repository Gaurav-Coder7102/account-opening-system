package com.bank.customer.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PanValidator implements ConstraintValidator<ValidPan, String> {

    // PAN format: 5 uppercase letters + 4 digits + 1 uppercase letter
    private static final String PAN_PATTERN = "^[A-Z]{5}[0-9]{4}[A-Z]{1}$";

    @Override
    public boolean isValid(String pan, ConstraintValidatorContext context) {
        if (pan == null || pan.isBlank()) {
            return false; // Let @NotBlank handle null/blank
        }
        return pan.matches(PAN_PATTERN);
    }
}
