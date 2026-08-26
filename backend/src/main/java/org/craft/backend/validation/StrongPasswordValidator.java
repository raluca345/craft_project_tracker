package org.craft.backend.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class StrongPasswordValidator implements ConstraintValidator<StrongPassword, String> {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null || value.isBlank()) {
            //leaving it to @NotBlank to avoid double errors
            return true;
        }

        return value.length() >= 8 && value.chars().anyMatch(Character::isLetterOrDigit);
    }
}
