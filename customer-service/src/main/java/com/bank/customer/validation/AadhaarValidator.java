package com.bank.customer.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class AadhaarValidator implements ConstraintValidator<ValidAadhaar, String> {

    // Aadhaar: exactly 12 digits, cannot start with 0 or 1
    private static final String AADHAAR_PATTERN = "^[2-9]{1}[0-9]{11}$";

    @Override
    public boolean isValid(String aadhaar, ConstraintValidatorContext context) {
        if (aadhaar == null || aadhaar.isBlank()) {
            return false; // Let @NotBlank handle null/blank
        }
        return aadhaar.matches(AADHAAR_PATTERN);
    }
}
