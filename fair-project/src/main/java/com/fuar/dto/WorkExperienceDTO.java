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
public class WorkExperienceDTO {
    private Long id;
    private String company;
    private String position;
    private String description;
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean current;
    private String location;
    private String companyUrl;
}
