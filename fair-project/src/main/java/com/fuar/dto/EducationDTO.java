package com.fuar.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class EducationDTO {
    private Long id;
    private String institution;
    private String fieldOfStudy;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean current;
    private String grade;
    private String activities;
}
