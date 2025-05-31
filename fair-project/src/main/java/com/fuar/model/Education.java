package com.fuar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "educations")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Education {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_info_id")
    @JsonIgnoreProperties({"workExperiences", "educations", "publications", "skills", "user", "hibernateLazyInitializer", "handler"})
    private UserInfo userInfo;

    @Column(nullable = false)
    private String institution;

    @Column(nullable = false)
    private String fieldOfStudy;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private LocalDate startDate;

    private LocalDate endDate;

    private boolean current;

    private String grade;

    @Column(columnDefinition = "TEXT")
    private String activities;
}
