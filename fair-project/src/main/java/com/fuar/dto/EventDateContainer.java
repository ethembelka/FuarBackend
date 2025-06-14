package com.fuar.dto;

import java.time.LocalDateTime;

/**
 * Interface for DTO classes that contain event date information
 */
public interface EventDateContainer {
    LocalDateTime getStartDate();
    LocalDateTime getEndDate();
}
