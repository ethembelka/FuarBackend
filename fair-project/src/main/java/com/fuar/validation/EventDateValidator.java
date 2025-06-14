package com.fuar.validation;

import com.fuar.dto.EventDateContainer;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EventDateValidator implements ConstraintValidator<ValidEventDate, EventDateContainer> {

    @Override
    public boolean isValid(EventDateContainer eventDateContainer, ConstraintValidatorContext context) {
        if (eventDateContainer.getStartDate() == null || eventDateContainer.getEndDate() == null) {
            return true; // Let @NotNull handle null values
        }
        
        // Always enforce that end date must be after start date
        if (!eventDateContainer.getEndDate().isAfter(eventDateContainer.getStartDate())) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("End date must be after start date")
                  .addPropertyNode("endDate")
                  .addConstraintViolation();
            return false;
        }
        
        return true;
    }
}
