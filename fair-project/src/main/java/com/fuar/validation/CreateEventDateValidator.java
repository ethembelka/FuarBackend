package com.fuar.validation;

import com.fuar.dto.EventDateContainer;
import com.fuar.dto.EventDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.time.LocalDateTime;

public class CreateEventDateValidator implements ConstraintValidator<CreateEventDateValidation, EventDTO> {

    @Override
    public boolean isValid(EventDTO eventDTO, ConstraintValidatorContext context) {
        // Skip validation if dates are null (let @NotNull handle that)
        if (eventDTO.getStartDate() == null || eventDTO.getEndDate() == null) {
            return true;
        }

        // Event updates should never use this validator, they should use @ValidEventDate only
        // This validator is for new event creation only
        if (eventDTO.getId() != null) {
            return true;
        }

        // Disable the default message
        context.disableDefaultConstraintViolation();
        
        // Only validate that end date is after start date
        boolean endDateValid = eventDTO.getEndDate().isAfter(eventDTO.getStartDate());
        
        if (!endDateValid) {
            context.buildConstraintViolationWithTemplate("End date must be after start date")
                  .addPropertyNode("endDate")
                  .addConstraintViolation();
            return false;
        }
        
        return true;
    }
}
