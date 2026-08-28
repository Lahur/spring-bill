package hr.bill.spring_bill.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = OibValidator.class)
@Target({ElementType.FIELD, ElementType.PARAMETER, ElementType.RECORD_COMPONENT})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidOib {
    String message() default "Invalid OIB";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
