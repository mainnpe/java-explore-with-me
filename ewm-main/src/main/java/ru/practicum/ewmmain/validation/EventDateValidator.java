package ru.practicum.ewmmain.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.time.LocalDateTime;

public class EventDateValidator implements ConstraintValidator<IsAfterConstraint, LocalDateTime> {
    private IsAfterConstraint constraint;

    @Override
    public void initialize(IsAfterConstraint constraintAnnotation) {
        constraint = constraintAnnotation;
        //ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(LocalDateTime dateTimeField, ConstraintValidatorContext context) {
        LocalDateTime referenceTime = LocalDateTime.now()
                .plusHours(constraint.offsetInHours());
        return !dateTimeField.isBefore(referenceTime);
    }
}
