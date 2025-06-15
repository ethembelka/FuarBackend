package com.fuar.validation;

import com.fuar.dto.EventDTO;
import com.fuar.dto.EventUpdateDTO;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Utility class for event validation logic
 */
@Component
public class EventValidationUtils {

    /**
     * Validates if an event is being created or updated
     * We only validate that end date is after start date
     *
     * @param eventDTO The event DTO to validate
     * @return true if validation passes, false otherwise
     */
    public boolean isValidEvent(EventDTO eventDTO) {
        if (eventDTO.getStartDate() == null || eventDTO.getEndDate() == null) {
            return false;
        }

        // Basic validation: end date must be after start date
        if (!eventDTO.getEndDate().isAfter(eventDTO.getStartDate())) {
            return false;
        }

        // No date restrictions for new or updated events - only validate end date is after start date
        return true;
    }
    
    /**
     * Validates an event update
     * For updates, we only check that end date is after start date
     *
     * @param eventUpdateDTO The event update DTO to validate
     * @return true if validation passes, false otherwise
     */
    public boolean isValidEventUpdate(EventUpdateDTO eventUpdateDTO) {
        if (eventUpdateDTO.getStartDate() == null || eventUpdateDTO.getEndDate() == null) {
            return false;
        }
        
        // For updates, we only validate that end date is after start date
        return eventUpdateDTO.getEndDate().isAfter(eventUpdateDTO.getStartDate());
    }
}
