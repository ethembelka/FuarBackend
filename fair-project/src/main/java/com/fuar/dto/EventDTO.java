package com.fuar.dto;

import com.fuar.validation.CreateEventDateValidation;
import com.fuar.validation.ValidEventDate;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
@CreateEventDateValidation
public class EventDTO implements EventDateContainer {
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotBlank(message = "Location is required")
    private String location;

    @NotNull(message = "Start date is required")
    private LocalDateTime startDate;

    @NotNull(message = "End date is required")
    private LocalDateTime endDate;

    @Min(value = 1, message = "Capacity must be at least 1")
    private Integer capacity;

    private String image;
    private Set<Long> speakerIds;
    private Set<Long> attendeeIds;
    private List<EventSessionDTO> sessions;
}
