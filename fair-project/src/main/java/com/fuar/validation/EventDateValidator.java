package com.fuar.validation;

import com.fuar.dto.EventDTO;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class EventDateValidator implements ConstraintValidator<ValidEventDate, EventDTO> {

    @Override
    public boolean isValid(EventDTO eventDTO, ConstraintValidatorContext context) {
        if (eventDTO.getStartDate() == null || eventDTO.getEndDate() == null) {
            return true; // Let @NotNull handle null values
        }
        return eventDTO.getEndDate().isAfter(eventDTO.getStartDate());
    }
}
