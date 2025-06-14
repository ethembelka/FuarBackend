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

        // For new events, enforce future dates
        LocalDateTime now = LocalDateTime.now();

        // Disable the default message
        context.disableDefaultConstraintViolation();
        
        // Check if start date is in the future
        boolean startDateValid = eventDTO.getStartDate().isAfter(now);
        if (!startDateValid) {
            context.buildConstraintViolationWithTemplate("Start date must be in the future for new events")
                  .addPropertyNode("startDate")
                  .addConstraintViolation();
        }
        
        // Check if end date is in the future and after start date
        boolean endDateValid = eventDTO.getEndDate().isAfter(now) && 
                               eventDTO.getEndDate().isAfter(eventDTO.getStartDate());
        if (!endDateValid) {
            if (!eventDTO.getEndDate().isAfter(now)) {
                context.buildConstraintViolationWithTemplate("End date must be in the future for new events")
                      .addPropertyNode("endDate")
                      .addConstraintViolation();
            } else if (!eventDTO.getEndDate().isAfter(eventDTO.getStartDate())) {
                context.buildConstraintViolationWithTemplate("End date must be after start date")
                      .addPropertyNode("endDate")
                      .addConstraintViolation();
            }
        }
        
        return startDateValid && endDateValid;
    }
}
