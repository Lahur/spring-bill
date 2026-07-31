package hr.bill.spring_bill.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class OibValidator implements ConstraintValidator<ValidOib, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (value == null) return true; // @NotNull handles null
        if (!value.matches("\\d{11}")) return false;

        int remainder = 10;
        for (int i = 0; i < 10; i++) {
            remainder = (remainder + (value.charAt(i) - '0')) % 10;
            if (remainder == 0) remainder = 10;
            remainder = (remainder * 2) % 11;
        }

        int controlDigit = 11 - remainder;
        if (controlDigit == 10) controlDigit = 0;

        return controlDigit == (value.charAt(10) - '0');
    }
}
